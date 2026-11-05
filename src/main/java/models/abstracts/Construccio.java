package models.abstracts;

import jakarta.persistence.*;
import models.Jugador;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Construccio {

    // L'id és de la jerarquia sencera: no hi pot haver dues construccions amb el mateix id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    // EAGER: una sola referència; a cada comprovació es mira de qui és la construcció
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    // Es defineix una sola vegada, a l'arrel, i les subclasses no el redefineixen.
    // L'id és únic a tota la jerarquia: un poblat i una carretera mai seran iguals.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Construccio other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    // Constant i de l'arrel: la classe d'un proxy és generada i donaria hashCode diferents
    @Override
    public int hashCode() {
        return Construccio.class.hashCode();
    }

    @Override
    public String toString() {
        return "Construccio{" +
                "id=" + id +
                ", jugadorId=" + (jugador != null ? jugador.getId() : null) +
                '}';
    }
}
