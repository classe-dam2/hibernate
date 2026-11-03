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
    
    @ManyToOne
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

    @Override
    public String toString() {
        return "Construccio{" +
                "id=" + id +
                ", jugador=" + jugador +
                '}';
    }
}
