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
    
    // LAZY: referència de tornada; s'hi arriba sempre des del jugador
    @ManyToOne(fetch = FetchType.LAZY)
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

    // Iguals si tenen el mateix id; una entitat nova (id null) només és igual a ella mateixa.
    // Es fa servir getId() i no el camp perquè en un proxy els camps són buits.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RecursJugador other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    // Constant: no pot canviar quan l'entitat rep l'id, ni dependre de la classe d'un proxy
    @Override
    public int hashCode() {
        return RecursJugador.class.hashCode();
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
