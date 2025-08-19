package isi.dan.msclientes.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import isi.dan.msclientes.enums.EstadoObra;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Testcontainers
@ActiveProfiles("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ObraRepositoryTest {

    Logger log = LoggerFactory.getLogger(ObraRepositoryTest.class);

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente cliente;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void iniciarDatos() {
        Obra obra = new Obra();
        obra.setDireccion("Test Obra 999");
        obra.setPresupuesto(BigDecimal.valueOf(100));
        obraRepository.save(obra);
    }

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setCorreoElectronico("juan.perez@email.com");
        cliente.setCuit("20-12345678-9");
        cliente.setMaximoDescubierto(BigDecimal.valueOf(50000));
        cliente = clienteRepository.save(cliente);
    }

    @BeforeEach
    void borrarDatos() {
        obraRepository.deleteAll();
    }

    @AfterAll
    static void stopContainer() {
        mysqlContainer.stop();
    }

    @Test
    void testSaveAndFindById() {
        Obra obra = new Obra();
        obra.setDireccion("Test Obra");
        obraRepository.save(obra);

        Optional<Obra> foundObra = obraRepository.findById(obra.getId());
        log.info("ENCONTRE: {} ", foundObra);
        assertThat(foundObra).isPresent();
        assertThat(foundObra.get().getDireccion()).isEqualTo("Test Obra");
    }

    @Test
    void testFindByPresupuesto() {
        Obra obra = new Obra();
        obra.setDireccion("Test Obra");
        obra.setPresupuesto(BigDecimal.valueOf(200));
        obraRepository.save(obra);

        List<Obra> resultado = obraRepository.findByPresupuestoGreaterThanEqual(BigDecimal.valueOf(50));
        log.info("ENCONTRE: {} ", resultado);
        assertThat(resultado.size()).isEqualTo(2);
        assertThat(resultado.get(0).getPresupuesto()).isGreaterThan(BigDecimal.valueOf(50));
        assertThat(resultado.get(1).getPresupuesto()).isGreaterThan(BigDecimal.valueOf(50));
    }

    @Test
    void testFindByCliente() {
        // Given
        Obra obra1 = new Obra();
        obra1.setEsRemodelacion(true);
        obra1.setPresupuesto(BigDecimal.valueOf(80000));
        obra1.setCliente(cliente);

        Obra obra2 = new Obra();
        obra2.setEsRemodelacion(false);
        obra2.setEstado(EstadoObra.FINALIZADA);
        obra2.setCliente(cliente);

        obraRepository.save(obra1);
        obraRepository.save(obra2);

        // When
        List<Obra> obras = obraRepository.findByCliente(cliente);

        // Then
        assertThat(obras).hasSize(2);
        assertThat(obras).extracting("direccion").containsExactlyInAnyOrder("San Martín 456", "Belgrano 789");
    }

    @Test
    void testFindByEstado() {
        // Given
        Obra obra1 = new Obra();
        obra1.setEsRemodelacion(false);
        obra1.setEstado(EstadoObra.PENDIENTE);
        obra1.setCliente(cliente);

        Obra obra2 = new Obra();
        obra2.setEsRemodelacion(true);
        obra2.setEstado(EstadoObra.PENDIENTE);
        obra2.setCliente(cliente);

        obraRepository.save(obra1);
        obraRepository.save(obra2);

        // When
        List<Obra> obrasPendientes = obraRepository.findByEstado(EstadoObra.PENDIENTE);

        // Then
        assertThat(obrasPendientes).hasSize(2);
        assertThat(obrasPendientes).allMatch(obra -> obra.getEstado() == EstadoObra.PENDIENTE);
    }

    @Test
    void testDeleteObra() {
        // Given
        Obra obra = new Obra();
        obra.setEsRemodelacion(false);
        obra.setCliente(cliente);
        Obra saved = obraRepository.save(obra);

        // When
        obraRepository.deleteById(saved.getId());

        // Then
        Optional<Obra> found = obraRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}
