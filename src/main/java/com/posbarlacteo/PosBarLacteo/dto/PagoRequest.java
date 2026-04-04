package com.posbarlacteo.PosBarLacteo.dto;

import java.util.List;

import com.posbarlacteo.PosBarLacteo.model.ItemVenta;

import lombok.Data;

@Data // Si usas Lombok, si no, usa los getters y setters que ya tienes
public class PagoRequest {
    private int monto;
    private List<ItemVenta> items;
}