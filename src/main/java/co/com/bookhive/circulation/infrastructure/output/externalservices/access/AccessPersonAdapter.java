package co.com.bookhive.circulation.infrastructure.output.externalservices.access;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.com.bookhive.access.services.input.dto.request.ChangePersonRequestDTO;
import co.com.bookhive.access.services.input.ports.ChangePersonInputPort;
import co.com.bookhive.circulation.services.output.ports.PersonAccessPort;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

@Component
public class AccessPersonAdapter implements PersonAccessPort {

	private final ChangePersonInputPort changePerson;

	public AccessPersonAdapter(final ChangePersonInputPort changePerson) {
		super();
		this.changePerson = changePerson;
	}

	@Override
	public void deactivate(final UUID person) {
		afterCommit(new ChangePersonRequestDTO(person, TenantContext.get(), ChangePersonRequestDTO.DEACTIVATE));
	}

	@Override
	public void reactivate(final UUID person) {
		afterCommit(new ChangePersonRequestDTO(person, TenantContext.get(), ChangePersonRequestDTO.REACTIVATE));
	}

	@Override
	public void anonymize(final UUID person) {
		afterCommit(new ChangePersonRequestDTO(person, TenantContext.get(), ChangePersonRequestDTO.ANONYMIZE));
	}

	private void afterCommit(final ChangePersonRequestDTO request) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			changePerson.execute(request);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				changePerson.execute(request);
			}

		});
	}

}
