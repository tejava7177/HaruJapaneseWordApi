package com.haru.api.buddy.init;

import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class BuddyRelationshipIntegrityLogger implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final BuddyRepository buddyRepository;
    private final TsunTsunRepository tsunTsunRepository;

    @Override
    public void run(String... args) {
        Long invalidBuddyRelationshipIds = jdbcTemplate.queryForObject("""
                select count(*)
                from buddy b
                left join buddy_relationship br on b.buddy_relationship_id = br.id
                where b.buddy_relationship_id is null
                   or b.buddy_relationship_id = 0
                   or br.id is null
                """, Long.class);

        Long invalidTsunTsunRelationshipIds = jdbcTemplate.queryForObject("""
                select count(*)
                from tsuntsun t
                left join buddy_relationship br on t.buddy_relationship_id = br.id
                where t.buddy_relationship_id is null
                   or t.buddy_relationship_id = 0
                   or br.id is null
                """, Long.class);

        log.info("[buddy/relationship] startup integrity: invalidBuddyRelationshipIdCount={}, invalidTsunTsunRelationshipIdCount={}, buddyRowsWithoutEntityRelationship={}, tsunTsunRowsWithoutEntityRelationship={}",
                invalidBuddyRelationshipIds,
                invalidTsunTsunRelationshipIds,
                buddyRepository.countWithMissingRelationship(),
                tsunTsunRepository.countWithMissingRelationship());
    }
}
