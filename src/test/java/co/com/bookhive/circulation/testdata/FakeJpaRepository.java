package co.com.bookhive.circulation.testdata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public final class FakeJpaRepository<T> implements InvocationHandler {

	private final List<T> stored;
	private final List<T> saved = new ArrayList<>();
	private final List<String> filters = new ArrayList<>();
	private final List<Pageable> pages = new ArrayList<>();
	private int changedRows = 1;

	private FakeJpaRepository(final List<T> stored) {
		this.stored = stored;
	}

	@SafeVarargs
	public static <T> FakeJpaRepository<T> storing(final T... entities) {
		return new FakeJpaRepository<>(List.of(entities));
	}

	public <R> R as(final Class<R> repositoryType) {
		return repositoryType.cast(Proxy.newProxyInstance(
				repositoryType.getClassLoader(), new Class<?>[] { repositoryType }, this));
	}

	public FakeJpaRepository<T> changingRows(final int rows) {
		changedRows = rows;
		return this;
	}

	public List<T> saved() {
		return saved;
	}

	public List<String> filters() {
		return filters;
	}

	public List<Pageable> pages() {
		return pages;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object invoke(final Object repository, final Method method, final Object[] arguments) {
		return switch (method.getName()) {
			case "save", "saveAndFlush" -> save((T) arguments[0]);
			case "findById" -> stored.stream().findFirst();
			case "findAll" -> findAll(arguments[0]);
			case "changeStatus" -> changeStatus(arguments);
			case "assign" -> assign(arguments);
			case "settle", "record" -> rowsChangedBy(method.getName(), arguments);
			case "hashCode" -> System.identityHashCode(repository);
			case "equals" -> repository == arguments[0];
			default -> "FakeJpaRepository";
		};
	}

	private T save(final T entity) {
		saved.add(entity);
		return entity;
	}

	private int changeStatus(final Object[] arguments) {
		filters.add(arguments[0] + ":" + arguments[1] + "->" + arguments[2]);
		return changedRows;
	}

	private int rowsChangedBy(final String operation, final Object[] arguments) {
		filters.add(operation + " " + Arrays.toString(arguments));
		return changedRows;
	}

	private int assign(final Object[] arguments) {
		filters.add(arguments[0] + ":" + arguments[4] + "->" + arguments[5]
				+ " ejemplar " + arguments[1] + " hasta " + arguments[3]);
		return changedRows;
	}

	@SuppressWarnings("unchecked")
	private Object findAll(final Object argument) {
		if (argument instanceof Pageable page) {
			pages.add(page);
			return new PageImpl<>(stored);
		}

		final Specification<T> specification = (Specification<T>) argument;
		specification.toPredicate(
				criteria(Root.class, ""),
				criteria(CriteriaQuery.class, ""),
				criteria(CriteriaBuilder.class, ""));
		return stored;
	}

	@SuppressWarnings("unchecked")
	private <C> C criteria(final Class<?> type, final String path) {
		return (C) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
				(instance, method, arguments) -> criteriaCall(path, method, arguments));
	}

	private Object criteriaCall(final String path, final Method method, final Object[] arguments) {
		final String name = method.getName();

		if ("get".equals(name)) {
			return criteria(Path.class, path.isEmpty() ? String.valueOf(arguments[0]) : path + "." + arguments[0]);
		}
		if ("equal".equals(name)) {
			filters.add(arguments[0] + "=" + arguments[1]);
		}
		if ("notEqual".equals(name)) {
			filters.add(arguments[0] + "!=" + arguments[1]);
		}
		if ("greaterThan".equals(name)) {
			filters.add(arguments[0] + ">" + arguments[1]);
		}
		if ("between".equals(name)) {
			filters.add(arguments[0] + " entre " + arguments[1] + " y " + arguments[2]);
		}
		if ("disjunction".equals(name)) {
			filters.add("ninguna");
		}
		if ("lessThan".equals(name)) {
			filters.add(arguments[0] + "<" + arguments[1]);
		}
		if ("in".equals(name)) {
			filters.add(path + " en " + arguments[0]);
		}
		if ("exists".equals(name)) {
			filters.add("existe la subconsulta");
		}
		if ("not".equals(name)) {
			filters.add("negada");
		}
		if ("lower".equals(name)) {
			return criteria(Expression.class, "lower(" + arguments[0] + ")");
		}
		if ("toString".equals(name)) {
			return path;
		}

		return method.getReturnType().isInterface() ? criteria(method.getReturnType(), path) : null;
	}

}
