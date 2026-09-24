package com.udo.can_cat.atenciones.domain.repository;

import com.udo.can_cat.atenciones.domain.entity.AtencionInsumo;
import java.util.List;

public interface AtencionInsumoRepository {

    List<AtencionInsumo> guardarTodos(List<AtencionInsumo> insumos);
    List<AtencionInsumo> buscarPorAtencionIds(List<Integer> idsAtencion);
}