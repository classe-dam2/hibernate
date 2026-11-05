## Autors

- **Adrià Cabrera**
- **Victoria Alcázar**

Alumnes de **2n de Desenvolupament d'Aplicacions Multiplataforma (DAM)**.

## Descripció del projecte

Aquest projecte consisteix a desenvolupar una versió simplificada del joc de taula *Els Colons de Catan* mitjançant **Java i Hibernate**, com a part de l'activitat de persistència de dades.

L'objectiu és gestionar una partida i les seves dades a través d'una base de dades, utilitzant entitats, relacions i consultes amb Hibernate/JPA.

**Curs:** 2026–2027  
**Cicle formatiu:** CFGS Desenvolupament d'Aplicacions Multiplataforma (DAM)

## DAO genèric

`saveOrUpdate(entitat)` retorna la còpia resultant de `merge`, inclòs l'ID generat;
l'objecte original no rep necessàriament aquests canvis. Les operacions CRUD
propaguen els errors després de fer rollback.

`get(id)` i `list()` retornen entitats desconnectades: les relacions lazy no
sol·licitades no es poden consultar després de tancar la sessió. Per carregar
atributs concrets abans del tancament, es poden indicar els seus noms directes,
per exemple `jugadorDao.get(id, "recursos")` o `jugadorDao.list("recursos")`.
Les relacions imbricades necessiten una consulta específica dins d'una transacció.
