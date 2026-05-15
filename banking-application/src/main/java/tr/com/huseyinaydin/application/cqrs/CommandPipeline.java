package tr.com.huseyinaydin.application.cqrs;

import java.util.List;

public final class CommandPipeline {

    private CommandPipeline() {}

    /**
     * Behavior chain'i ters sırada sararak terminal handler'a ulaşır.
     * Davranışlar @Order değerine göre önceden sıralanmış olmalıdır.
     * CQRS mimarisinde handler, gelen command veya query’nin gerçek iş mantığını
     * çalıştıran merkez noktadır ve normalde doğrudan çağrılır gibi görünse de,
     * AOP (Aspect Oriented Programming) sayesinde bu çağrı aslında bir “proxy/interceptor”
     * katmanından geçer; framework (örneğin Spring) reflection ve proxy mekanizmalarını
     * kullanarak handler çağrısını sarar ve handler’a ulaşmadan önce araya girerek loglama,
     * caching, güvenlik kontrolü, validation veya metrik toplama gibi cross-cutting
     * concern işlemlerini çalıştırır, yani istek handler’a gitmeden önce “dur bir bakayım”
     * denilen bir ara katmana uğrar, burada gerekli işlemler yapılır ve ardından zincir
     * devam ettirilerek gerçek handler’a iletilir, böylece iş mantığı temiz kalır ve
     * tekrar eden altyapı kodları merkezi bir şekilde yönetilmiş olur.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    // Bu metot, request’i direkt handler’a vermek yerine önce araya “kontrol katmanları” koyarak çalıştırır
    public static <TRequest, TResult> TResult execute(
            TRequest request,
            List<IPipelineBehavior<?, ?>> behaviors,
            PipelineDelegate<TResult> terminal) {
        // En son çalışacak olan gerçek iş yapan kısım (CQRS handler)
        PipelineDelegate<TResult> chain = terminal;

        // Behaviors ters çevrilerek dolaşılır çünkü ilk eklenen en dışta çalışmalıdır
        for (int i = behaviors.size() - 1; i >= 0; i--) {
            // O anki kontrol katmanı (log, validation, cache gibi işler)
            IPipelineBehavior behavior = behaviors.get(i);
            // Mevcut zincirin bir sonraki adımı saklanır
            PipelineDelegate<TResult> next = chain;
            // Yeni bir katman oluşturulur:
            // Bu katman önce kendi işini yapar, sonra sıradaki adıma geçer
            chain = () -> (TResult) behavior.handle(request, next);
        }
        // Oluşturulan tüm katmanlar çalıştırılır ve request zincirden geçer
        return chain.proceed();
    }

    /*
    Bu metot, gelen request’i doğrudan CQRS handler’a göndermek yerine önce bir
    pipeline zinciri kurar; en altta gerçek iş mantığını yapan terminal handler
    başlatıcı olarak alınır, ardından behaviors listesi tersten dolaşılarak her
    bir cross-cutting concern (log, validation, cache, security vb.) bir önceki
    zinciri saran bir katmana dönüştürülür ve böylece her behavior hem request’i
    işleyebilir hem de kontrolü bir sonraki adıma devredebilir, en sonunda
    oluşturulan bu iç içe sarılmış yapı chain.proceed() ile tetiklenir ve request
    sırasıyla tüm ara katmanlardan geçerek en sonda CQRS handler’a ulaşır ve sonuç üretilir.
     */
}
