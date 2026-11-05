package models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import models.abstracts.Construccio;
import models.enums.ColorJugador;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.DynamicInsert;

@NamedQuery(name = "Jugador.perPartida",
        query = "from Jugador j where j.partida.id = :idPartida order by j.ordreTorn")
@Entity
@Table(name = "jugador")
@Check(constraints = "ordreTorn between 1 and 4 and cavallersJugats >= 0")
@DynamicInsert
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull()
    @NotBlank()
    @Column(name = "nom", nullable = false, length = 25)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 10)
    private ColorJugador color;

    @Min(value = 1, message = "L'ordre del torn no pot ser menor a u")
    @Max(value = 4, message = "L'ordre del torn no pot ser major a quatre")
    @Column(name = "ordreTorn", nullable = false)
    private Integer ordreTorn;

    @Min(value = 0, message = "cavallersJugats no pot ser menor a cero")
    @Column(name = "cavallersJugats", nullable = false)
    private Integer cavallersJugats = 0;

    // EAGER: gairebé totes les accions del jugador consulten la partida (torn, estat, tauler)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    // LAZY: només cal per als punts i per jugar cartes; els DAO la llegeixen dins de la sessió
    @OneToMany(mappedBy = "jugador", fetch = FetchType.LAZY)
    private List<CartaDesenvolupament> cartes = new ArrayList<>();
    
    // LAZY: es llegeix dins de la sessió quan cal comprovar o modificar recursos
    @OneToMany(mappedBy = "jugador", fetch = FetchType.LAZY)
    private List<RecursJugador> recursos = new ArrayList<>();
    
    // LAZY: creix durant la partida; els recomptes es fan des del tauler de la partida
    @OneToMany(mappedBy = "jugador", fetch = FetchType.LAZY)
    private List<Construccio> construccions = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ColorJugador getColor() {
        return color;
    }

    public void setColor(ColorJugador color) {
        this.color = color;
    }

    public Integer getOrdreTorn() {
        return ordreTorn;
    }

    public void setOrdreTorn(Integer ordreTorn) {
        this.ordreTorn = ordreTorn;
    }

    public Integer getCavallersJugats() {
        return cavallersJugats;
    }

    public void setCavallersJugats(Integer cavallersJugats) {
        this.cavallersJugats = cavallersJugats;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public List<CartaDesenvolupament> getCartes() {
        return cartes;
    }

    public void setCartes(List<CartaDesenvolupament> cartes) {
        this.cartes = cartes;
    }

    public List<RecursJugador> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<RecursJugador> recursos) {
        this.recursos = recursos;
    }

    // Iguals si tenen el mateix id; una entitat nova (id null) només és igual a ella mateixa.
    // Es fa servir getId() i no el camp perquè en un proxy els camps són buits.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Jugador other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    // Constant: no pot canviar quan l'entitat rep l'id, ni dependre de la classe d'un proxy
    @Override
    public int hashCode() {
        return Jugador.class.hashCode();
    }

    // Sense col·leccions: podrien provocar LazyInitializationException o recursió infinita
    @Override
    public String toString() {
        return "Jugador{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", color=" + color +
                ", ordreTorn=" + ordreTorn +
                ", cavallersJugats=" + cavallersJugats +
                ", partidaId=" + (partida != null ? partida.getId() : null) +
                '}';
    }

    public List<Construccio> getConstruccions() {
        return construccions;
    }

    public void setConstruccions(List<Construccio> construccions) {
        this.construccions = construccions;
    }
}
