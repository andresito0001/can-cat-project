package com.udo.can_cat.atenciones.domain.repository;

import com.udo.can_cat.atenciones.domain.entity.RecetaItem;
import java.util.List;

public interface RecetaItemRepository {

    List<RecetaItem> guardarTodos(List<RecetaItem> items);
    List<RecetaItem> buscarPorRecetaId(Integer idReceta);
}