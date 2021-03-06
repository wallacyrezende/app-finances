package com.dev.finances.service.impl;

import com.dev.finances.exception.BusinessException;
import com.dev.finances.model.entity.Release;
import com.dev.finances.model.entity.User;
import com.dev.finances.model.enums.ReleaseStatusEnum;
import com.dev.finances.model.enums.ReleaseTypeEnum;
import com.dev.finances.repository.ReleaseRepository;
import com.dev.finances.service.ReleaseService;
import com.dev.finances.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.dev.finances.model.repository.ReleaseRepositoryTest.createRelease;
import static com.dev.finances.service.UserServiceTest.createUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Release service tests")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ReleaseServiceImplTest {

	public static final String ENTER_A_VALID_VALUE = "Informe um Valor válido.";
	public static final String ENTER_A_TYPE_RELEASE = "Informe um Tipo de lançamento.";
	public static final String ENTER_A_RELEASE_DATE = "Informe uma Data para o lançamento.";
	public static final String ENTER_A_VALID_DESCRIPTION = "Informe uma Descrição válida.";
	public static final String ENTER_A_VALID_USER = "Informe um Usuário.";

	ReleaseRepository releaseRepository;
	ReleaseService releaseService;
	UserService userService;


	@BeforeEach
	public void setUp() {
		releaseRepository = Mockito.mock(ReleaseRepository.class);
		userService = Mockito.mock(UserService.class);
		releaseService = new ReleaseServiceImpl(userService, releaseRepository);
	}
	
	@Test
	@DisplayName("MUST CREATE a new release")
	public void mustCreateNewRelease() {

		Release releaseToSave = createRelease();
		User user = createUser();

		Release releaseCreated = createRelease();
		releaseCreated.setStatus(ReleaseStatusEnum.PENDENTE);
		releaseCreated.setCreateAt(LocalDate.of(2022, 01, 01));
		Mockito.when(releaseRepository.save(releaseToSave)).thenReturn(releaseCreated);

		Release release = releaseService.save(releaseToSave);

		assertNotNull(release);
		assertEquals(release.getStatus(), ReleaseStatusEnum.PENDENTE);
		assertEquals(release.getCreateAt(), LocalDate.of(2022, 01, 01));
		assertEquals(release.getUser(), user);
	}

	@Test
	@DisplayName("MUST NOT CREATE a release when is an error in the validation")
	public void mustNotCreateAReleaseWhenIsAnErrorInTheValidation() {
		Release release = createRelease();
		release.setUser(null);
		assertThrows(BusinessException.class, () -> releaseService.save(release));
		Mockito.verify(releaseRepository, Mockito.never()).save(release);
	}

	@Test
	@DisplayName("MUST UPDATE a release")
	public void mustUpdateARelease() {
		Release releaseUpdated = createRelease();
		releaseUpdated.setId(1L);

		Mockito.when(releaseRepository.save(Mockito.any( Release.class ))).thenReturn(releaseUpdated);

		releaseService.update(releaseUpdated);

		Mockito.verify(releaseRepository, Mockito.times(1)).save(releaseUpdated);
	}

	@Test
	@DisplayName("MUST THROW ERROR trying to update a release that has not yet saved")
	public void mustThrowErrorTryingToUpdateAReleaseThatHasNotYetSaved() {
		Release release = createRelease();

		assertThrows(NullPointerException.class, () -> releaseService.update(release));

		Mockito.verify(releaseRepository, Mockito.never()).save(release);
	}

	@Test
	@DisplayName("MUST DELETE a release")
	public void mustDeleteARelease() {
		Release release = createRelease();
		release.setId(1L);

		releaseService.delete(release);

		Mockito.verify(releaseRepository).delete(release);
	}

	@Test
	@DisplayName("MUST THROW ERROR trying to delete a release that has not yet saved")
	public void mustThrowErrorTryingToDeleteAReleaseThatHasNotYetSaved() {
		Release release = createRelease();

		assertThrows(NullPointerException.class, () -> releaseService.delete(release));

		Mockito.verify(releaseRepository, Mockito.never()).delete(release);
	}

	@Test
	@DisplayName("MUST FILTER releases")
	public void mustFilterReleases() {
		Release release = createRelease();
		release.setId(1L);

		List<Release> list = Arrays.asList(release);
		Mockito.when( releaseRepository.findAll(Mockito.any( Example.class ))).thenReturn(list);

		List<Release> result = releaseService.find(release);

		assertFalse(result.isEmpty());
		assertFalse(result.size() > 1);
		assertTrue(result.contains(release));
	}

	@Test
	@DisplayName("MUST UPDATE STATUS of the release")
	public void mustUpdateStatusOfTheRelease() {
		Release release = createRelease();
		release.setId(1L);
		release.setStatus(ReleaseStatusEnum.PENDENTE);

		ReleaseStatusEnum newStatus = ReleaseStatusEnum.EFETIVADO;
		Mockito.when(releaseService.update(release)).thenReturn(release);

		releaseService.updateStatus(release, newStatus);

		assertEquals(release.getStatus(), newStatus);
		Mockito.verify(releaseRepository).save(release);
	}

	@Test
	@DisplayName("MUST GET release by ID")
	public void mustGetReleaseByID() {
		Long id = 1L;
		Release release = createRelease();
		release.setId(id);

		Mockito.when(releaseRepository.findById(id)).thenReturn(Optional.of(release));

		Optional<Release> releaseFound = releaseService.findById(id);

		assertTrue(releaseFound.isPresent());
	}

	@Test
	@DisplayName("MUST return EMPTY when is not exists release")
	public void mustReturnEmptyWhenIsNotExistsRelease() {
		Long id = 1L;
		Release release = createRelease();
		release.setId(id);

		Mockito.when(releaseRepository.findById(id)).thenReturn(Optional.empty());

		Optional<Release> releaseFound = releaseService.findById(id);

		assertFalse(releaseFound.isPresent());
	}

	@Test
	@DisplayName("MUST THROW errors when validating a release")
	public void mustThrowErrorsWhenValidatingARelease() {
		Release  release =  new Release();

		BusinessException error = assertThrows( BusinessException.class, () -> releaseService.validate(release) );
		assertEquals(error.getMessage(), ENTER_A_VALID_DESCRIPTION);

		release.setDescription("Salário");
		error = assertThrows( BusinessException.class, () -> releaseService.validate(release) );
		assertEquals(error.getMessage(), ENTER_A_VALID_USER);

		release.setUser(createUser());
		error = assertThrows( BusinessException.class, () -> releaseService.validate(release) );
		assertEquals(error.getMessage(), ENTER_A_VALID_VALUE);

		release.setValue(BigDecimal.ONE);
		error = assertThrows( BusinessException.class, () -> releaseService.validate(release) );
		assertEquals(error.getMessage(), ENTER_A_TYPE_RELEASE);

		release.setType(ReleaseTypeEnum.RECEITA);
		error = assertThrows( BusinessException.class, () -> releaseService.validate(release) );
		assertEquals(error.getMessage(), ENTER_A_RELEASE_DATE);
	}
}
