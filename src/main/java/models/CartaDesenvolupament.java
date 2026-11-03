package models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import models.enums.TipusCarta;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "cartaDesenvolupament")
@DynamicInsert
public class CartaDesenvolupament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 20)
    private TipusCarta tipus;
    
    @Column(name = "usada", nullable = false)
    private boolean usada;

    @Min(value = 0, message = "rondaCompra no pot ser menor a cero")
    @Column(name = "rondaCompra", nullable = false)
    private Integer rondaCompra;
    
    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;
    
    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipusCarta getTipus() {
        return tipus;
    }

    public void setTipus(TipusCarta tipus) {
        this.tipus = tipus;
    }

    public boolean isUsada() {
        return usada;
    }

    public void setUsada(boolean usada) {
        this.usada = usada;
    }

    public Integer getRondaCompra() {
        return rondaCompra;
    }

    public void setRondaCompra(Integer rondaCompra) {
        this.rondaCompra = rondaCompra;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    @Override
    public String toString() {
        return "CartaDesenvolupament{" +
                "id=" + id +
                ", tipus=" + tipus +
                ", usada=" + usada +
                ", rondaCompra=" + rondaCompra +
                ", jugadorId=" + (jugador != null ? jugador.getId() : null) +
                ", partidaId=" + (partida != null ? partida.getId() : null) +
                '}';
    }
}
