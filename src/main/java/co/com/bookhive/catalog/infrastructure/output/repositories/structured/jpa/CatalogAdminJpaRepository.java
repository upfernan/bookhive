package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities.BookJpaEntity;

public interface CatalogAdminJpaRepository extends Repository<BookJpaEntity, UUID> {

	interface CategoryRow {

		UUID getId();

		String getName();

		String getStatus();

		Long getTitles();

	}

	interface TitleRow {

		UUID getId();

		String getTitle();

		String getIsbn();

		Integer getYear();

		String getFormat();

		String getStatus();

		UUID getCategory();

		String getPublisher();

		String getAuthors();

	}

	@Query(value = "SELECT c.id AS id, c.name AS name, c.status AS status,"
			+ " (SELECT count(*) FROM book b WHERE b.category_id = c.id AND b.status = 'ACTIVO') AS titles"
			+ " FROM category c ORDER BY lower(c.name)", nativeQuery = true)
	List<CategoryRow> findCategories();

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO category (id, name) VALUES (:id, :name)", nativeQuery = true)
	int createCategory(@Param("id") UUID id, @Param("name") String name);

	@Modifying(flushAutomatically = true)
	@Query(value = "UPDATE category SET name = :name WHERE id = :id", nativeQuery = true)
	int renameCategory(@Param("id") UUID id, @Param("name") String name);

	@Modifying(flushAutomatically = true)
	@Query(value = "UPDATE category SET status = :status WHERE id = :id", nativeQuery = true)
	int changeCategoryStatus(@Param("id") UUID id, @Param("status") String status);

	@Query(value = "SELECT b.id AS id, b.title AS title, b.isbn AS isbn, CAST(b.publication_year AS integer) AS year,"
			+ " b.format AS format, b.status AS status, b.category_id AS category, p.name AS publisher,"
			+ " string_agg(a.name, chr(31) ORDER BY a.name) AS authors"
			+ " FROM book b JOIN publisher p ON p.id = b.publisher_id"
			+ " LEFT JOIN book_author ba ON ba.book_id = b.id LEFT JOIN author a ON a.id = ba.author_id"
			+ " WHERE b.id = :id GROUP BY b.id, p.name", nativeQuery = true)
	List<TitleRow> findTitle(@Param("id") UUID id);

	@Query(value = "SELECT count(*) FROM book WHERE isbn = :isbn AND id <> :id", nativeQuery = true)
	long countIsbn(@Param("isbn") String isbn, @Param("id") UUID except);

	@Query(value = "SELECT id FROM publisher WHERE lower(name) = lower(:name) ORDER BY id LIMIT 1", nativeQuery = true)
	List<UUID> findPublisher(@Param("name") String name);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO publisher (id, name) VALUES (:id, :name)", nativeQuery = true)
	int createPublisher(@Param("id") UUID id, @Param("name") String name);

	@Query(value = "SELECT id FROM author WHERE lower(name) = lower(:name) ORDER BY id LIMIT 1", nativeQuery = true)
	List<UUID> findAuthor(@Param("name") String name);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO author (id, name) VALUES (:id, :name)", nativeQuery = true)
	int createAuthor(@Param("id") UUID id, @Param("name") String name);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO book (id, title, isbn, publication_year, format, status, category_id, publisher_id)"
			+ " VALUES (:id, :title, NULLIF(:isbn, ''), CAST(NULLIF(:year, 0) AS smallint), :format, 'ACTIVO', :category, :publisher)",
			nativeQuery = true)
	int createBook(@Param("id") UUID id, @Param("title") String title, @Param("isbn") String isbn, @Param("year") int year,
			@Param("format") String format, @Param("category") UUID category, @Param("publisher") UUID publisher);

	@Modifying(flushAutomatically = true)
	@Query(value = "UPDATE book SET title = :title, isbn = NULLIF(:isbn, ''), publication_year = CAST(NULLIF(:year, 0) AS smallint),"
			+ " category_id = :category, publisher_id = :publisher WHERE id = :id", nativeQuery = true)
	int updateBook(@Param("id") UUID id, @Param("title") String title, @Param("isbn") String isbn, @Param("year") int year,
			@Param("category") UUID category, @Param("publisher") UUID publisher);

	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM book_author WHERE book_id = :book", nativeQuery = true)
	int deleteAuthors(@Param("book") UUID book);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO book_author (book_id, author_id) VALUES (:book, :author)", nativeQuery = true)
	int addAuthor(@Param("book") UUID book, @Param("author") UUID author);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "UPDATE book SET status = 'BAJA' WHERE id = :id AND status = 'ACTIVO'", nativeQuery = true)
	int withdrawBook(@Param("id") UUID id);

}
