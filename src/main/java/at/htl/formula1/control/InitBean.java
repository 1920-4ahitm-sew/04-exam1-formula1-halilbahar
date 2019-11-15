package at.htl.formula1.control;

import at.htl.formula1.boundary.ResultsRestClient;
import at.htl.formula1.entity.Driver;
import at.htl.formula1.entity.Race;
import at.htl.formula1.entity.Team;

import javax.ejb.Local;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Transactional
@ApplicationScoped
public class InitBean {

    private static final String TEAM_FILE_NAME = "teams.csv";
    private static final String RACES_FILE_NAME = "races.csv";

    @PersistenceContext
    EntityManager em;

    @Inject
    ResultsRestClient client;


    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {

        readTeamsAndDriversFromFile(TEAM_FILE_NAME);
        readRacesFromFile(RACES_FILE_NAME);
        client.readResultsFromEndpoint();

    }

    /**
     * Einlesen der Datei "races.csv" und Speichern der Objekte in der Tabelle F1_RACE
     *
     * @param racesFileName
     */
    private void readRacesFromFile(String racesFileName) {
        try {
            InputStreamReader streamReader = new InputStreamReader(this.getClass().getResourceAsStream("/" + racesFileName));
            BufferedReader reader = new BufferedReader(streamReader);
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] singleRow = line.split(";");
                long id = Long.parseLong(singleRow[0]);
                String country = singleRow[1];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate date = LocalDate.parse(singleRow[2], formatter);
                this.em.persist(new Race(id, country, date));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Einlesen der Datei "teams.csv".
     * Das String-Array jeder einzelnen Zeile wird der Methode persistTeamAndDrivers(...)
     * übergeben
     *
     * @param teamFileName
     */
    private void readTeamsAndDriversFromFile(String teamFileName) {
        try {
            InputStreamReader streamReader = new InputStreamReader(this.getClass().getResourceAsStream("/" + teamFileName));
            BufferedReader reader = new BufferedReader(streamReader);
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] singleRow = line.split(";");
                this.persistTeamAndDrivers(singleRow);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Es wird überprüft ob es das übergebene Team schon in der Tabelle F1_TEAM gibt.
     * Falls nicht, wird das Team in der Tabelle gespeichert.
     * Wenn es das Team schon gibt, dann liest man das Team aus der Tabelle und
     * erstellt ein Objekt (der Klasse Team).
     * Dieses Objekt wird verwendet, um die Fahrer mit Ihrem jeweiligen Team
     * in der Tabelle F!_DRIVER zu speichern.
     *
     * @param line String-Array mit den einzelnen Werten der csv-Datei
     */
    private void persistTeamAndDrivers(String[] line) {
        List<Team> resultList = this.em
                .createNamedQuery("team.getByName", Team.class)
                .setParameter("TEAM", line[0])
                .getResultList();
        Team currentTeam;
        if (resultList.size() != 1) {
            currentTeam = new Team(line[0]);
            this.em.persist(currentTeam);
        } else {
            currentTeam = resultList.get(0);
        }
        this.em.persist(new Driver(line[1], currentTeam));
        this.em.persist(new Driver(line[2], currentTeam));
    }
}
