package models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import models.enums.TipusRecurs;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "recursJugador", uniqueConstraints = @UniqueConstraint(columnNames = {"id_jugador", "tipus"}))
@Check(constraints = "quantitat >= 0")
@DynamicInsert
public class RecursJugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 10)
    private TipusRecurs tipus;
    
    @Min(value = 0, message = "quantitat no pot ser menor a cero")
    @Column(name = "quantitat", nullable = false)
    private Integer quantitat;
    
    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_jugador", nullable = false)
	private Jugador jugador;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipusRecurs getTipus() {
        return tipus;
    }

    public void setTipus(TipusRecurs tipus) {
        this.tipus = tipus;
    }

    public Integer getQuantitat() {
        return quantitat;
    }

    public void setQuantitat(Integer quantitat) {
        this.quantitat = quantitat;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    @Override
    public String toString() {
        return "RecursJugador{" +
                "id=" + id +
                ", tipus=" + tipus +
                ", quantitat=" + quantitat +
                ", jugadorId=" + (jugador != null ? jugador.getId() : null) +
                '}';
    }
}
