package isi.dan.ms.pedidos.dao;

import java.util.List;

import isi.dan.ms.pedidos.modelo.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

  List<Pedido> findByClienteIdAndEstadoIn(Long clienteId, List<Pedido.EstadoPedido> estados);
  
  List<Pedido> findByClienteId(Long clienteId);
  
  List<Pedido> findByEstado(Pedido.EstadoPedido estado);
  
  List<Pedido> findByClienteIdAndEstado(Long clienteId, Pedido.EstadoPedido estado);
  
  // Métodos para filtrar por lista de clientes (userId)
  List<Pedido> findByClienteIdIn(List<Long> clienteIds);
  
  List<Pedido> findByClienteIdInAndEstado(List<Long> clienteIds, Pedido.EstadoPedido estado);
}

