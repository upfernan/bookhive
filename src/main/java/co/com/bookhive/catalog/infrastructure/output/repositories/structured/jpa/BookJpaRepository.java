package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities.BookJpaEntity;

@Repository
public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

	String AUTHOR_SEPARATOR = "\u001F";

	String BOOK_DETAILS = "SELECT b.id AS id, b.title AS title, b.isbn AS isbn,"
			+ " CAST(b.publication_year AS integer) AS year, b.format AS format, b.status AS status,"
			+ " c.name AS category, p.name AS publisher,"
			+ " string_agg(a.name, chr(31) ORDER BY a.name) AS authors"
			+ " FROM book b"
			+ " JOIN category c ON c.id = b.category_id"
			+ " JOIN publisher p ON p.id = b.publisher_id"
			+ " LEFT JOIN book_author ba ON ba.book_id = b.id"
			+ " LEFT JOIN author a ON a.id = ba.author_id";

	String GROUP_BY_BOOK = " GROUP BY b.id, c.name, p.name";

	@Query(value = BOOK_DETAILS + " WHERE b.status = :status" + GROUP_BY_BOOK
			+ " ORDER BY b.id LIMIT :size OFFSET :offset", nativeQuery = true)
	List<BookDetailsRow> findDetailsByStatus(
			@Param("status") String status,
			@Param("size") int size,
			@Param("offset") long offset);

	@Query(value = BOOK_DETAILS + " WHERE b.id IN (:ids)" + GROUP_BY_BOOK, nativeQuery = true)
	List<BookDetailsRow> findDetailsByIds(@Param("ids") Collection<UUID> ids);

	long countByStatus(String status);

}
