package tr.com.huseyinaydin.application.ports;

/**
 * İsteği yapan istemcinin IP adresini sağlayan port. Implementasyonu sunum katmanındadır
 * (web: HttpServletRequest'ten okur). IP belirlenemezse null döner.
 */
public interface IpAddressProvider {
    String getClientIpAddress();
}
