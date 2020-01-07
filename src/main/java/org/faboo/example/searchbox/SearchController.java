package org.faboo.example.searchbox;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.neo4j.driver.Values.parameters;


@RestController
public class SearchController {

    private final Driver driver;

    public SearchController(Driver driver) {
        this.driver = driver;
    }


    @GetMapping(path = "/searchUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Object> searchUsers(@RequestParam(value="term") String term) {

        try (Session session = driver.session()) {
            return session.run(
                    "match (u:User) where u.description starts with $searchterm and u.pagerank is not null return u as user order by u.pagerank desc limit 5 \n" +
                            "UNION\n" +
                            "CALL db.index.fulltext.queryNodes('desc', $searchterm + '~') YIELD node, score with node as user where user.pagerank is not null \n" +
                            "return user order by user.pagerank desc limit 5",
                    parameters("searchterm", term))
                    .list(r -> r.get("user").asMap());
        }
    }
}
