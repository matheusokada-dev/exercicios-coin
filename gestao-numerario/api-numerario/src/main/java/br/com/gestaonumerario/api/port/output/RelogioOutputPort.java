package br.com.gestaonumerario.api.port.output;

import java.time.Instant;
import java.time.LocalDate;

public interface RelogioOutputPort {

    Instant agora();

    LocalDate hoje();
}
