package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;

public interface LocationJpaRepository extends Repository<BookCopyJpaEntity, UUID> {

	interface BranchRow {

		UUID getId();

		String getName();

	}

	interface DepthRow {

		Integer getDepth();

		String getText();

	}

	@Query(value = "SELECT id, name FROM branch ORDER BY lower(name)", nativeQuery = true)
	List<BranchRow> findBranches();

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO branch (id, name) VALUES (:id, :name)", nativeQuery = true)
	int createBranch(@Param("id") UUID id, @Param("name") String name);

	@Query(value = "SELECT depth, name AS text FROM location_level ORDER BY depth", nativeQuery = true)
	List<DepthRow> findLevels();

	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM location_level WHERE depth > :depth", nativeQuery = true)
	int deleteLevelsDeeperThan(@Param("depth") int depth);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO location_level (depth, name) VALUES (:depth, :name)"
			+ " ON CONFLICT (depth) DO UPDATE SET name = EXCLUDED.name", nativeQuery = true)
	int saveLevel(@Param("depth") int depth, @Param("name") String name);

	@Query(value = "SELECT count(*) FROM book_copy_location WHERE depth > :depth", nativeQuery = true)
	long countValuesDeeperThan(@Param("depth") int depth);

	@Query(value = "SELECT depth, value AS text FROM book_copy_location WHERE book_copy_id = :copy ORDER BY depth",
			nativeQuery = true)
	List<DepthRow> findValues(@Param("copy") UUID copy);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "UPDATE book_copy SET branch_id = :branch WHERE id = :copy", nativeQuery = true)
	int changeBranch(@Param("copy") UUID copy, @Param("branch") UUID branch);

	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM book_copy_location WHERE book_copy_id = :copy", nativeQuery = true)
	int deleteValues(@Param("copy") UUID copy);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO book_copy (id, book_id, branch_id, barcode, status, lendable)"
			+ " VALUES (:id, :book, :branch, :barcode, 'DISPONIBLE', true)", nativeQuery = true)
	int saveBookCopy(@Param("id") UUID id, @Param("book") UUID book, @Param("branch") UUID branch,
			@Param("barcode") String barcode);

	@Query(value = "SELECT count(*) FROM book_copy WHERE barcode IN (:barcodes)", nativeQuery = true)
	long countBarcodes(@Param("barcodes") List<String> barcodes);

	@Modifying(flushAutomatically = true)
	@Query(value = "INSERT INTO book_copy_location (book_copy_id, depth, value) VALUES (:copy, :depth, :value)",
			nativeQuery = true)
	int saveValue(@Param("copy") UUID copy, @Param("depth") int depth, @Param("value") String value);

}
