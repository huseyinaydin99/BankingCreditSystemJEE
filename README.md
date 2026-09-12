# Kurumsal Kredi Yönetim Sistemi: Mimari Bir Başkaldırı

Bu satırları okuyan aziz meslektaşım, karşında duran bu proje sıradan bir kod yığını yahut birilerinin hafta sonu hevesiyle karaladığı alelade bir uygulama değildir. Günümüz yazılım dünyasının içine düştüğü "hızlıca teslim edelim, arkasını sonra düşünürüz" girdabına ve framework'lerin esiri olmuş tembel zihniyetlere karşı verilmiş kati bir mimari başkaldırıdır. 

Bu eser, nizamın, intizamın ve sürdürülebilirliğin koda dökülmüş bir tecellisidir. Kodun her bir satırı, ne yaptığını bilen, sınırlarına vakıf ve görevini bihakkın yerine getiren bir yapı üzerine bina edilmiştir.

---

## Projemiz Ne Mahiyettedir, Ne Değildir?

Bu proje, bankacılık ve finans sektörünün en kritik operasyonlarından biri olan **Kredi Başvuru ve Tahsis Süreçlerini** baştan uca yöneten kurumsal bir Java (Enterprise Java) çözümüdür. Bireysel ve kurumsal müşterilerin kayıt altına alınmasından, karmaşık faiz hesaplamalarına, onay/ret mekanizmalarından, zamanlanmış hatırlatıcı ve temizlik görevlerine kadar koca bir iş akışını ihtiva eder.

**Ne değildir?** Katiyetle "spaghetti" kod tabir edilen, her katmanın birbirine girdiği, veritabanı nesnelerinin kullanıcı arayüzüne kadar sızdığı o kokuşmuş MVC projelerinden değildir. Bu proje, "Clean Architecture" (Temiz Mimari) fıtratına uygun doğmuş, dış dünya ile olan bağlarını portlar ve adaptörler (Hexagonal) vasıtasıyla kuran, merkezinde saf iş mantığından (Domain) başka hiçbir otorite tanımayan izole bir kaledir.

## Hangi Müşküllere Deva Olur?

Yazılım projeleri büyüdükçe, başlangıçta masum görünen küçük tavizler zamanla devasa bir "Teknik Borç" dağına dönüşür. Bu proje tam da bu zaafiyetleri hedef alır:
* **Framework Bağımlılığı:** Geleneksel sistemlerde kod, kullanılan framework'e öylesine göbekten bağlanır ki, yarın öbür gün framework değiştiğinde tüm sistem çöker. Bizim yapımızda ise Çekirdek (Domain), Spring'in yahut Hibernate'in varlığından dahi haberdar değildir.
* **Karmaşık İş Kurallarının Yönetimi:** Yüzlerce satırlık if-else blokları yerine, "Pipeline Behavior" ve "Business Rules" nesneleri ile kurallar şeffaf, test edilebilir ve sarih bir biçimde ele alınmıştır.
* **Okuma/Yazma Darboğazları:** Veri okuma ile veri yazma eylemlerinin yükleri ve ihtiyaçları birbirinden farklıdır. Bu müşkül, CQRS şablonu ile kesin bir dille ayrıştırılmış ve sistemin nefes alması sağlanmıştır.

## Kime ve Neye Hizmet Eder?

Bu sistem, veri tutarlılığının (consistency) hayati önem taşıdığı **finans kuruluşlarına, bankalara ve kredi tahsis kurumlarına** hizmet etmek gayesiyle tasarlanmıştır. Aynı zamanda, "gerçek bir kurumsal mimari nasıl inşa edilir" sorusuna yanıt arayan, zanaatına hürmet eden yazılım mühendisleri için bir referans külliyatı niteliğindedir.

---

## Kullanım Usulü (Nizam-ı Çalışma)

Projenin derlenmesi ve ayağa kaldırılması, standart maven yaşam döngüsüne sadık kalınarak tasarlanmıştır. 

1. **Derleme:** Kök dizinde `mvn clean install` komutu icra edildiğinde, yedi farklı modül kendi içerisindeki hiyerarşiye (Shared-Kernel -> Domain -> Application -> Infrastructure -> Persistence -> Web) sadık kalarak, tüm testlerini (unit, integration, pipeline behavior) koşar ve derlenir.
2. **Profillerin Tatbiki (Environment Management):** Sistem; `dev`, `test` ve `prod` olmak üzere üç farklı profile sahiptir. Dışarıdan enjekte edilen ortam değişkenleri (`BANKING_DB_URL`, `BANKING_JWT_SECRET_KEY` vb.) aracılığıyla yapılandırılır. Geliştirme ortamında (dev) H2 in-memory veritabanı ayağa kalkarken, prod ortamında sıkı güvenlikli ve performanslı HikariCP havuzu üzerinden Oracle veritabanına bağlanır. 
3. **Çalıştırma:** Nihai olarak `banking-web` modülünden elde edilen `war` yahut doğrudan başlatılabilir yapı, herhangi bir uygulama sunucusunda (Tomcat vb.) şaha kalkmaya hazırdır.

---

## Mimari Felsefe ve Tasarım Şablonları

Sistemin temelleri atılırken, geçici heveslerden ziyade zamanın sınavından başarıyla geçmiş köklü tasarım şablonları (Design Patterns) tercih edilmiştir.

* **Clean Architecture & Hexagonal Architecture:** Sistemin kalbinde (Domain) sadece iş kuralları ve saf Java nesneleri yer alır. Veritabanı, Web veya güvenlik mekanizmaları en dış katmanlara itilmiştir. Böylelikle iş mantığı, teknolojinin kölesi olmaktan kurtarılmıştır.
* **CQRS (Command Query Responsibility Segregation) & Mediator:** Sisteme gelen her bir istek, ya sistemi değiştiren bir "Command" yahut veri okuyan bir "Query" olarak ele alınır. Özelleştirilmiş `SpringMediator` yapımız sayesinde, bu komutlar doğrudan doğruya ilgili işleyicilerine (Handler) ulaştırılır. Controller sınıfları sadece Mediator'a emir vermekle mükelleftir; arka plandaki hengameden bihaberdirler.
* **Pipeline Behaviors (Boru Hattı Davranışları):** Tıpkı bir fabrikadaki üretim bandı gibi, her bir komut hedefine ulaşmadan evvel; `LoggingBehavior` (Kayıt), `ValidationBehavior` (Doğrulama), `AuthorizationBehavior` (Yetkilendirme) ve `TransactionBehavior` (İşlem Bütünlüğü) duraklarından geçer. Araya giren bu yapılar (Interceptor benzeri), kod tekrarını (AOP mantığıyla) kökünden kazımıştır.
* **Repository & Specification Pattern:** Veri erişim işlemleri soyutlanmış olup, karmaşık sorgular "Specification" arayüzü ile ifade edilerek domain dilinin veritabanı dillerine ezdirilmesinin önüne geçilmiştir.

---

## Kullanılan Teknolojiler ve Esbab-ı Mucibesi (Seçim Nedenleri)

Her bir kütüphane veya teknoloji, öylesine değil, derin bir muhakeme neticesinde projeye dahil edilmiştir.

| Teknoloji / Araç | Kullanım Amacı ve Esbab-ı Mucibesi (Neden Seçildi?) |
| :--- | :--- |
| **Java 17** | Projenin ana lisanıdır. Sunduğu record'lar, sealed class'lar ve gelişmiş bellek yönetimi (ZGC vb.) sayesinde kurumsal bir yapının ihtiyaç duyduğu hızı ve modernliği katiyetle sağlar. |
| **Spring Framework (Core, WebMVC, Security)** | Projenin bel kemiğini oluşturmasına rağmen, ipleri tamamen eline almasına (Boot auto-config tembelliğine) müsaade edilmemiştir. Bağımlılıkların enjekte edilmesi (DI) ve Web/Güvenlik katmanlarının tesis edilmesi hususunda, kendi yazdığımız konfigürasyonlarla dizginlenerek kullanılmıştır. |
| **Hibernate & JPA (XML Mapping ile)** | Veritabanı işlemlerini icra etmekle görevlidir. Ancak dikkat buyurun: Domain katmanındaki varlık (Entity) sınıflarımızda tek bir tane bile `@Entity` veya `@Column` göremezsiniz. ORM haritalaması tamamen dışarıda, `META-INF/orm` dizinindeki XML dosyaları vasıtasıyla yapılmıştır ki Domain katmanımız kirlenmesin, saf kalsın! |
| **Quartz & Spring Scheduling** | Sistemin arka planında sessizce bekleyen, zamanı geldiğinde şahlanıp süresi dolan başvuruları iptal eden veya hatırlatma maillerini yollayan gece bekçilerimizdir. |
| **Caffeine Cache** | Veritabanına lüzumsuz yere mükerrer yüklenmelerin önüne geçmek, özellikle sık değişmeyen kredi türleri gibi verileri bellekte muhafaza etmek gayesiyle seçilmiş, fevkalade hızlı bir önbellekleme mekanizmasıdır. |
| **Bucket4j** | Kötü niyetli zevatın API'lerimize fütursuzca saldırıp sistemi boğmasını engellemek amacıyla eklenmiş olan, saniye/dakika bazlı "Rate Limiting" (hız sınırlama) kalkanımızdır. |
| **JJWT & Security Claims** | Kullanıcı kimlik doğrulamasında token tabanlı, devletsiz (stateless) bir nizam kurmak için kullanılmıştır. Rol ve özel "Operation Claims" (yetki etiketleri) mantığıyla, mikroskobik seviyede ince yetkilendirme (fine-grained authorization) yapabilmemize olanak tanır. |
| **Resilience4j (Circuit Breaker & Retry)** | Dış dünya ile konuşurken (örneğin e-posta yollarken) yaşanabilecek sarsıntıları tolere edebilmek, sistemin komple çökmesine mâni olmak için devre kesici ve yeniden deneme şemsiyesi olarak entegre edilmiştir. |

---

## Algoritmalar ve Kök İş Mantığı

Sistem içerisinde basit CRUD işlemlerinden çok daha fazlası cereyan eder. İş mantığının kalbinde yatan algoritmalar şunlardır:

1. **Amortisman ve Faiz Hesaplama (Compound Interest Algoritması):** Kredi onaylandığı anda, müşterinin talep ettiği vade ve faiz oranına istinaden `BigDecimal` hassasiyetiyle (RoundingMode.HALF_UP ile) aylık ve toplam geri ödeme tutarları formülize edilerek saniyesinde hesaplanır. Para mevzubahis olduğunda yuvarlama hatalarına katiyen tahammül edilemez.
2. **Kriptografik ve Şekilsel Doğrulamalar:** T.C. Kimlik Numarası modül-10 checksum algoritması ve Vergi Kimlik Numarası algoritmaları, sisteme kirli verinin girmesini daha en dış kapıda, `ValidationBehavior` seviyesinde engeller.
3. **Domain Events (Etki Alanı Olayları):** Bir kredi onaylandığında yahut reddedildiğinde, süreç sadece durumun güncellenmesiyle bitmez. Nesnenin kendi içinden fırlattığı `CreditApplicationApprovedEvent` gibi olaylar, Event Listener'lar tarafından yakalanarak asenkron e-posta gönderimi gibi yan etkilere (side effects) güvenli bir şekilde delege edilir.

## Hâtime

Velhasıl kelam, elinizde tuttuğunuz bu kod tabanı, bir yazılım projesinden çok, yazılım mimarisine duyulan saygının bir tezahürüdür. Gelecekte projeyi devralacak olan meslektaşlarımın bu yapının fıtratını bozmadan, aynı özen ve nizam ile kod yazmaya devam etmeleri en büyük temennimdir. 

Kodunuzun derlenmesinde hata, mimarinizde zaafiyet olmasın, amin.

Saygılarımla...

Hüseyin AYDIN