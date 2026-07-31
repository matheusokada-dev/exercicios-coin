package br.com.gestaonumerario.api.adapter.output.time;

import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class SystemRelogioAdapter implements RelogioOutputPort {

    @Override
    public Instant agora() {
        return Instant.now();
    }

    @Override
    public LocalDate hoje() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}
