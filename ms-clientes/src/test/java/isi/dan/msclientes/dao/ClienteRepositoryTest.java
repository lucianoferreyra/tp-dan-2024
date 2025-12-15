// package isi.dan.msclientes.dao;

// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.springframework.test.context.junit.jupiter.SpringExtension;
// import org.testcontainers.containers.MySQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;

// import isi.dan.msclientes.model.Cliente;

// import java.math.BigDecimal;
// import java.util.Optional;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;

// @ExtendWith(SpringExtension.class)
// @DataJpaTest
// @Testcontainers
// @ActiveProfiles("db")
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// public class ClienteRepositoryTest {
//   Logger log = LoggerFactory.getLogger(ClienteRepositoryTest.class);

//   @Container
//   public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
//       .withDatabaseName("testdb")
//       .withUsername("test")
//       .withPassword("test");

//   @Autowired
//   private ClienteRepository clienteRepository;

//   @DynamicPropertySource
//   static void configureProperties(DynamicPropertyRegistry registry) {
//     registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
//     registry.add("spring.datasource.username", mysqlContainer::getUsername);
//     registry.add("spring.datasource.password", mysqlContainer::getPassword);
//   }

//   @BeforeEach
//   void borrarDatos() {
//     clienteRepository.deleteAll();
//   }

//   @AfterAll
//   static void stopContainer() {
//     mysqlContainer.stop();
//   }

//   @Test
//   void testSaveAndFindCliente() {
//     // Given
//     Cliente cliente = new Cliente();
//     cliente.setNombre("Juan");
//     cliente.setCorreoElectronico("juan.perez@email.com");
//     cliente.setCuit("20-12345678-9");
//     cliente.setMaximoDescubierto(BigDecimal.valueOf(10000));

//     // When
//     Cliente savedCliente = clienteRepository.save(cliente);

//     // Then
//     assertThat(savedCliente.getId()).isNotNull();
//     assertThat(savedCliente.getNombre()).isEqualTo("Juan");
//   }

//   @Test
//   void testFindByEmail() {
//     // Given
//     Cliente cliente = new Cliente();
//     cliente.setNombre("María");
//     cliente.setCorreoElectronico("maria.gonzalez@email.com");
//     cliente.setCuit("27-98765432-1");
//     cliente.setMaximoDescubierto(BigDecimal.valueOf(5000));
//     clienteRepository.save(cliente);

//     // When
//     Optional<Cliente> found = clienteRepository.findByCorreoElectronico("maria.gonzalez@email.com");

//     // Then
//     assertThat(found).isPresent();
//     assertThat(found.get().getNombre()).isEqualTo("María");
//   }

//   @Test
//   void testFindByCuit() {
//     // Given
//     Cliente cliente = new Cliente();
//     cliente.setNombre("Carlos");
//     cliente.setCorreoElectronico("carlos.lopez@email.com");
//     cliente.setCuit("23-11111111-9");
//     cliente.setMaximoDescubierto(BigDecimal.valueOf(15000));
//     clienteRepository.save(cliente);

//     // When
//     Optional<Cliente> found = clienteRepository.findByCuit("23-11111111-9");

//     // Then
//     assertThat(found).isPresent();
//   }

//   @Test
//   void testDeleteCliente() {
//     // Given
//     Cliente cliente = new Cliente();
//     cliente.setNombre("Ana");
//     cliente.setCorreoElectronico("ana.martin@email.com");
//     cliente.setCuit("24-22222222-8");
//     cliente.setMaximoDescubierto(BigDecimal.valueOf(8000));
//     Cliente saved = clienteRepository.save(cliente);

//     // When
//     clienteRepository.deleteById(saved.getId());

//     // Then
//     Optional<Cliente> found = clienteRepository.findById(saved.getId());
//     assertThat(found).isEmpty();
//   }

//   @Test
//   void testFindAllClientes() {
//     // Given
//     Cliente cliente1 = new Cliente();
//     cliente1.setNombre("Pedro");
//     cliente1.setCorreoElectronico("pedro.ramirez@email.com");
//     cliente1.setCuit("20-33333333-7");
//     cliente1.setMaximoDescubierto(BigDecimal.valueOf(12000));

//     Cliente cliente2 = new Cliente();
//     cliente2.setNombre("Laura");
//     cliente2.setCorreoElectronico("laura.fernandez@email.com");
//     cliente2.setCuit("27-44444444-6");
//     cliente2.setMaximoDescubierto(BigDecimal.valueOf(7000));

//     clienteRepository.save(cliente1);
//     clienteRepository.save(cliente2);

//     // When
//     List<Cliente> clientes = clienteRepository.findAll();

//     // Then
//     assertThat(clientes).hasSize(2);
//     assertThat(clientes).extracting("nombre").containsExactlyInAnyOrder("Pedro", "Laura");
//   }
// }
