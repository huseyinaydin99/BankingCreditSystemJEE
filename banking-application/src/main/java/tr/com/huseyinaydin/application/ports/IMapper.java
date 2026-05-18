package tr.com.huseyinaydin.application.ports;

public interface IMapper {

    <S, D> D map(S source, Class<D> destinationType);
}
