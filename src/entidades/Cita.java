package entidades;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Data
@ToString(exclude = {"paciente", "medico", "sala"})
@Builder
public class Cita implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Paciente paciente;
    private final Medico medico;
    private final Sala sala;
    private final LocalDateTime fechaHora;
    private final BigDecimal costo;
    @Builder.Default
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    @Builder.Default
    private String observaciones = "";

    public Cita(Paciente paciente,
                Medico medico,
                Sala sala,
                LocalDateTime fechaHora,
                BigDecimal costo,
                EstadoCita estado,
                String observaciones) {

        this.paciente = Objects.requireNonNull(paciente, "El paciente no puede ser nulo");
        this.medico = Objects.requireNonNull(medico, "El médico no puede ser nulo");
        this.sala = Objects.requireNonNull(sala, "La sala no puede ser nula");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora no pueden ser nulas");
        this.costo = Objects.requireNonNull(costo, "El costo no puede ser nulo");
        this.estado = estado != null ? estado : EstadoCita.PROGRAMADA;
        this.observaciones = observaciones != null ? observaciones : "";
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                paciente.getDni(),
                medico.getDni(),
                sala.getNumero(),
                fechaHora.toString(),
                costo.toString(),
                estado.name(),
                observaciones.replaceAll(",", ";"));
    }

    public static Cita fromCsvString(String csvString,
                                     Map<String, Paciente> pacientes,
                                     Map<String, Medico> medicos,
                                     Map<String, Sala> salas) throws CitaException {
        String[] values = csvString.split(",");
        if (values.length != 7) {
            throw new CitaException("Formato de CSV inválido para Cita: " + csvString);
        }

        Paciente paciente = pacientes.get(values[0]);
        Medico medico = medicos.get(values[1]);
        Sala sala = salas.get(values[2]);
        if (paciente == null || medico == null || sala == null) {
            throw new CitaException("Datos de referencia faltantes");
        }

        return Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .sala(sala)
                .fechaHora(LocalDateTime.parse(values[3]))
                .costo(new BigDecimal(values[4]))
                .estado(EstadoCita.valueOf(values[5]))
                .observaciones(values[6].replaceAll(";", ","))
                .build();
    }
}
