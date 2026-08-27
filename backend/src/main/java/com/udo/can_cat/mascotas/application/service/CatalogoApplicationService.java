package com.udo.can_cat.mascotas.application.service;

import java.util.List;
import com.udo.can_cat.mascotas.application.dto.EspecieDTO;
import com.udo.can_cat.mascotas.application.dto.RazaDTO;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;
import com.udo.can_cat.mascotas.domain.repository.EspecieRepository;
import com.udo.can_cat.mascotas.domain.repository.RazaRepository;
import org.springframework.stereotype.Service;

@Service
public class CatalogoApplicationService {

    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;

    public CatalogoApplicationService(EspecieRepository especieRepository,
                                      RazaRepository razaRepository) {
        this.especieRepository = especieRepository;
        this.razaRepository = razaRepository;
    }

    public List<EspecieDTO> listarEspecies() {
        return especieRepository.findAll().stream()
                .map(e -> new EspecieDTO(e.getId().value(), e.getNombre(), e.getDescripcion()))
                .toList();
    }

    public List<RazaDTO> listarRazasPorEspecie(Integer idEspecie) {
        return razaRepository.findByEspecieId(new EspecieId(idEspecie)).stream()
                .map(r -> new RazaDTO(
                        r.getId().value(),
                        r.getEspecieId().value(),
                        r.getNombre(),
                        r.getCaracteristicas()))
                .toList();
    }
}