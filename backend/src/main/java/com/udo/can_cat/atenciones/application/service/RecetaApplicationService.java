package com.udo.can_cat.atenciones.application.service;

import com.udo.can_cat.atenciones.application.dto.RecetaPdfDTO;
import com.udo.can_cat.atenciones.application.port.MascotaPort;
import com.udo.can_cat.atenciones.application.port.PersonalPort;
import com.udo.can_cat.atenciones.domain.entity.AtencionClinica;
import com.udo.can_cat.atenciones.domain.entity.Receta;
import com.udo.can_cat.atenciones.domain.entity.RecetaItem;
import com.udo.can_cat.atenciones.domain.exception.RecetaNoEncontradaException;
import com.udo.can_cat.atenciones.domain.repository.AtencionClinicaRepository;
import com.udo.can_cat.atenciones.domain.repository.RecetaItemRepository;
import com.udo.can_cat.atenciones.domain.repository.RecetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
public class RecetaApplicationService {

    private static final Logger log = LoggerFactory.getLogger(RecetaApplicationService.class);

    private final RecetaRepository recetaRepo;
    private final RecetaItemRepository recetaItemRepo;
    private final AtencionClinicaRepository atencionRepo;
    private final MascotaPort mascotaPort;
    private final PersonalPort personalPort;

    public RecetaApplicationService(RecetaRepository recetaRepo,
                                    RecetaItemRepository recetaItemRepo,
                                    AtencionClinicaRepository atencionRepo,
                                    MascotaPort mascotaPort,
                                    PersonalPort personalPort) {
        this.recetaRepo = recetaRepo;
        this.recetaItemRepo = recetaItemRepo;
        this.atencionRepo = atencionRepo;
        this.mascotaPort = mascotaPort;
        this.personalPort = personalPort;
    }

    /** 404 si el récipe no existe (RecetaNoEncontradaException → handler). */
    @Transactional(readOnly = true)
    public RecetaPdfDTO descargarReceta(Integer idReceta) {
        Receta receta = recetaRepo.buscarPorId(new Receta.RecetaId(idReceta))
                .orElseThrow(() -> new RecetaNoEncontradaException(idReceta));

        List<RecetaItem> items = recetaItemRepo.buscarPorRecetaId(idReceta).stream()
                .sorted(Comparator.comparing(RecetaItem::getOrden,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        AtencionClinica atencion = (receta.getIdAtencion() != null)
                ? atencionRepo.buscarPorId(new AtencionClinica.AtencionId(receta.getIdAtencion())).orElse(null)
                : null;

        PersonalPort.PersonalInfo veterinario = (atencion != null && atencion.getIdVeterinario() != null)
                ? personalPort.buscarPorId(atencion.getIdVeterinario()).orElse(null)
                : null;

        MascotaPort.MascotaInfo mascota = (atencion != null && atencion.getIdMascota() != null)
                ? mascotaPort.buscarPorId(atencion.getIdMascota()).orElse(null)
                : null;

        log.debug("PDF de récipe {}: {} ítems", receta.getCodigoReceta(), items.size());

        return new RecetaPdfDTO(
                receta.getId() != null ? receta.getId().value() : idReceta,
                receta.getCodigoReceta(),
                receta.getFechaEmision(),
                atencion != null ? atencion.getFechaHoraInicio() : null,
                atencion != null ? atencion.getDiagnosticoPrincipal() : null,
                receta.getIndicacionesGenerales(),
                veterinario != null ? veterinario.nombreCompleto() : null,
                veterinario != null ? veterinario.especialidad() : null,
                veterinario != null ? veterinario.licenciaProfesional() : null,
                mascota != null ? mascota.nombre() : null,
                mascota != null ? mascota.especie() : null,
                mascota != null ? mascota.raza() : null,
                mascota != null ? mascota.sexo() : null,
                mascota != null ? mascota.clienteNombre() : null,
                mascota != null ? mascota.clienteDocumento() : null,
                items.stream()
                        .map(i -> new RecetaPdfDTO.ItemPdfDTO(
                                i.getOrden(), i.getMedicamento(), i.getConcentracion(),
                                i.getDosis(), i.getViaAdministracion(), i.getFrecuencia(),
                                i.getDuracion()))
                        .toList());
    }
}