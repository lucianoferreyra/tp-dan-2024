// package isi.dan.msclientes.dao;

// import isi.dan.msclientes.model.Cliente;
// import isi.dan.msclientes.model.Usuario;
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

// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;

// @ExtendWith(SpringExtension.class)
// @DataJpaTest
// @Testcontainers
// @ActiveProfiles("db")
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// public class UsuarioRepositoryTest {
    
//     Logger log = LoggerFactory.getLogger(UsuarioRepositoryTest.class);

//     @Container
//     public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
//             .withDatabaseName("testdb")
//             .withUsername("test")
//             .withPassword("test");

//     @Autowired
//     private UsuarioRepository usuarioRepository;

//     @Autowired
//     private ClienteRepository clienteRepository;

//     @DynamicPropertySource
//     static void configureProperties(DynamicPropertyRegistry registry) {
//         registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
//         registry.add("spring.datasource.username", mysqlContainer::getUsername);
//         registry.add("spring.datasource.password", mysqlContainer::getPassword);
//     }

//     private Cliente clienteTest;

//     @BeforeEach
//     void setUp() {
//         usuarioRepository.deleteAll();
//         clienteRepository.deleteAll();

//         // Crear un cliente de prueba
//         clienteTest = new Cliente();
//         clienteTest.setNombre("Empresa Test");
//         clienteTest.setCorreoElectronico("empresa@test.com");
//         clienteTest.setCuit("20-12345678-9");
//         clienteTest.setMaximoDescubierto(BigDecimal.valueOf(10000));
//         clienteTest = clienteRepository.save(clienteTest);
//     }

//     @AfterAll
//     static void stopContainer() {
//         mysqlContainer.stop();
//     }

//     @Test
//     void testSaveAndFindUsuario() {
//         // Given
//         Usuario usuario = new Usuario();
//         usuario.setNombre("Juan");
//         usuario.setApellido("Pérez");
//         usuario.setDni("12345678");
//         usuario.setCorreoElectronico("juan.perez@test.com");
//         usuario.setCliente(clienteTest);

//         // When
//         Usuario savedUsuario = usuarioRepository.save(usuario);

//         // Then
//         assertThat(savedUsuario.getId()).isNotNull();
//         assertThat(savedUsuario.getNombre()).isEqualTo("Juan");
//         assertThat(savedUsuario.getApellido()).isEqualTo("Pérez");
//         assertThat(savedUsuario.getCliente().getId()).isEqualTo(clienteTest.getId());
//     }

//     @Test
//     void testFindByClienteId() {
//         // Given
//         Usuario usuario1 = new Usuario();
//         usuario1.setNombre("Juan");
//         usuario1.setApellido("Pérez");
//         usuario1.setDni("12345678");
//         usuario1.setCorreoElectronico("juan.perez@test.com");
//         usuario1.setCliente(clienteTest);
//         usuarioRepository.save(usuario1);

//         Usuario usuario2 = new Usuario();
//         usuario2.setNombre("María");
//         usuario2.setApellido("González");
//         usuario2.setDni("87654321");
//         usuario2.setCorreoElectronico("maria.gonzalez@test.com");
//         usuario2.setCliente(clienteTest);
//         usuarioRepository.save(usuario2);

//         // When
//         List<Usuario> usuarios = usuarioRepository.findByClienteId(clienteTest.getId());

//         // Then
//         assertThat(usuarios).hasSize(2);
//         assertThat(usuarios).extracting(Usuario::getNombre).containsExactlyInAnyOrder("Juan", "María");
//     }

//     @Test
//     void testFindByDni() {
//         // Given
//         Usuario usuario = new Usuario();
//         usuario.setNombre("Juan");
//         usuario.setApellido("Pérez");
//         usuario.setDni("12345678");
//         usuario.setCorreoElectronico("juan.perez@test.com");
//         usuario.setCliente(clienteTest);
//         usuarioRepository.save(usuario);

//         // When
//         Optional<Usuario> found = usuarioRepository.findByDni("12345678");

//         // Then
//         assertThat(found).isPresent();
//         assertThat(found.get().getNombre()).isEqualTo("Juan");
//         assertThat(found.get().getApellido()).isEqualTo("Pérez");
//     }

//     @Test
//     void testFindByCorreoElectronico() {
//         // Given
//         Usuario usuario = new Usuario();
//         usuario.setNombre("María");
//         usuario.setApellido("González");
//         usuario.setDni("87654321");
//         usuario.setCorreoElectronico("maria.gonzalez@test.com");
//         usuario.setCliente(clienteTest);
//         usuarioRepository.save(usuario);

//         // When
//         Optional<Usuario> found = usuarioRepository.findByCorreoElectronico("maria.gonzalez@test.com");

//         // Then
//         assertThat(found).isPresent();
//         assertThat(found.get().getNombre()).isEqualTo("María");
//     }

//     @Test
//     void testExistsByDni() {
//         // Given
//         Usuario usuario = new Usuario();
//         usuario.setNombre("Juan");
//         usuario.setApellido("Pérez");
//         usuario.setDni("12345678");
//         usuario.setCorreoElectronico("juan.perez@test.com");
//         usuario.setCliente(clienteTest);
//         usuarioRepository.save(usuario);

//         // When
//         boolean exists = usuarioRepository.existsByDni("12345678");
//         boolean notExists = usuarioRepository.existsByDni("99999999");

//         // Then
//         assertThat(exists).isTrue();
//         assertThat(notExists).isFalse();
//     }

//     @Test
//     void testExistsByCorreoElectronico() {
//         // Given
//         Usuario usuario = new Usuario();
//         usuario.setNombre("Juan");
//         usuario.setApellido("Pérez");
//         usuario.setDni("12345678");
//         usuario.setCorreoElectronico("juan.perez@test.com");
//         usuario.setCliente(clienteTest);
//         usuarioRepository.save(usuario);

//         // When
//         boolean exists = usuarioRepository.existsByCorreoElectronico("juan.perez@test.com");
//         boolean notExists = usuarioRepository.existsByCorreoElectronico("noexiste@test.com");

//         // Then
//         assertThat(exists).isTrue();
//         assertThat(notExists).isFalse();
//     }

//     @Test
//     void testDeleteUsuario() {
//         // Given
//         Usuario usuario = new Usuario();
//         usuario.setNombre("Juan");
//         usuario.setApellido("Pérez");
//         usuario.setDni("12345678");
//         usuario.setCorreoElectronico("juan.perez@test.com");
//         usuario.setCliente(clienteTest);
//         Usuario saved = usuarioRepository.save(usuario);

//         // When
//         usuarioRepository.deleteById(saved.getId());
//         Optional<Usuario> found = usuarioRepository.findById(saved.getId());

//         // Then
//         assertThat(found).isEmpty();
//     }
// }
