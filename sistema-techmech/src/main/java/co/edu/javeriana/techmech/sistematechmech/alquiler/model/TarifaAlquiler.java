package co.edu.javeriana.techmech.sistematechmech.alquiler.model;

import java.math.BigDecimal;

public class TarifaAlquiler {
    private Long idTarifa;
    private String tipoVehiculo;
    private BigDecimal tarifaDiaria;
    private BigDecimal depositoGarantia;
    private String descripcion;
    private boolean activo;

    public TarifaAlquiler(Long idTarifa, String tipoVehiculo, BigDecimal tarifaDiaria, 
                          BigDecimal depositoGarantia, String descripcion, boolean activo) {
        this.idTarifa = idTarifa;
        this.tipoVehiculo = tipoVehiculo;
        this.tarifaDiaria = tarifaDiaria;
        this.depositoGarantia = depositoGarantia;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Long getIdTarifa() { 
        return idTarifa; 
    }
    
    public void setIdTarifa(Long idTarifa) { 
        this.idTarifa = idTarifa; 
    }

    public String getTipoVehiculo() { 
        return tipoVehiculo; 
    }
    
    public void setTipoVehiculo(String tipoVehiculo) { 
        this.tipoVehiculo = tipoVehiculo; 
    }

    public BigDecimal getTarifaDiaria() { 
        return tarifaDiaria; 
    }
    
    public void setTarifaDiaria(BigDecimal tarifaDiaria) { 
        this.tarifaDiaria = tarifaDiaria; 
    }

    public BigDecimal getDepositoGarantia() { 
        return depositoGarantia; 
    }
    
    public void setDepositoGarantia(BigDecimal depositoGarantia) { 
        this.depositoGarantia = depositoGarantia; 
    }

    public String getDescripcion() { 
        return descripcion; 
    }
    
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }

    public boolean isActivo() { 
        return activo; 
    }
    
    public void setActivo(boolean activo) { 
        this.activo = activo; 
    }
}