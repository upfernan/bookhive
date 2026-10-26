package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.services.output.ports.BranchDirectoryPort;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.circulation.services.input.ports.ListBranchesInputPort;

@Component
public class CirculationBranchDirectoryAdapter implements BranchDirectoryPort {

	private final ListBranchesInputPort listBranches;

	public CirculationBranchDirectoryAdapter(final ListBranchesInputPort listBranches) {
		super();
		this.listBranches = listBranches;
	}

	@Override
	public String nameOf(final UUID branch) {
		return listBranches.execute().stream()
				.filter(found -> found.id().equals(branch))
				.map(LocationResponseDTOs.Branch::name)
				.findFirst()
				.orElse("");
	}

}
