package models.abstracts;

import jakarta.persistence.*;
import models.Aresta;

@Entity
public class Carretera extends Construccio {

    @OneToOne
    @JoinColumn(name = "aresta_id", unique = true, nullable = false)
    private Aresta aresta;

    public Aresta getAresta() {
        return aresta;
    }

    public void setAresta(Aresta aresta) {
        this.aresta = aresta;
        if (aresta != null) {
            aresta.setCarretera(this);
        }
    }

    @Override
    public String toString() {
        return "Carretera{" +
                "aresta=" + aresta +
                '}';
    }
}
