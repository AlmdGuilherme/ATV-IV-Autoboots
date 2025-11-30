package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.enumeracoes.UserRoles;
import com.autobots.automanager.modelos.AdicionadorLinkVenda;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vendas")
public class VendaControle {
    @Autowired
    private RepositorioVenda repositorioVenda;
    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private AdicionadorLinkVenda adicionadorLinkVenda;

    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return repositorioUsuario.findByEmailEspecifico(email).orElseThrow();
    }

    private boolean isAdminOuGerente(Usuario usuario) {
        return usuario.getNivelAcesso().contains(UserRoles.ROLE_ADMIN) ||
                usuario.getNivelAcesso().contains(UserRoles.ROLE_GERENTE);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Venda>> listarVendas() {
        List<Venda> vendas = repositorioVenda.findAll();
        if (vendas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkVenda.adicionadorLinkGeral(vendas);
            return new ResponseEntity<>(vendas, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Venda> buscarVendaPorId(@PathVariable long id) {
        Usuario logado = getUsuarioLogado();
        Optional<Venda> vendaOpt = repositorioVenda.findById(id);

        if (vendaOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Venda venda = vendaOpt.get();

        if (isAdminOuGerente(logado)) {
            adicionadorLinkVenda.adicionadorLink(venda);
            return new ResponseEntity<>(venda, HttpStatus.OK);
        }

        if (logado.getNivelAcesso().contains(UserRoles.ROLE_VENDEDOR)) {
            if (venda.getFuncionario() != null && venda.getFuncionario().getId().equals(logado.getId())) {
                adicionadorLinkVenda.adicionadorLink(venda);
                return new ResponseEntity<>(venda, HttpStatus.OK);
            }
        }

        if (logado.getNivelAcesso().contains(UserRoles.ROLE_CLIENTE)) {
            if (venda.getCliente() != null && venda.getCliente().getId().equals(logado.getId())) {
                adicionadorLinkVenda.adicionadorLink(venda);
                return new ResponseEntity<>(venda, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Venda> cadastrarVenda(@RequestBody Venda venda) {
        if (venda != null) {
            Usuario logado = getUsuarioLogado();

            if (logado.getNivelAcesso().contains(UserRoles.ROLE_VENDEDOR) && !isAdminOuGerente(logado)) {
                venda.setFuncionario(logado);
            }

            venda.setCadastro(new Date());
            repositorioVenda.save(venda);
            return new ResponseEntity<>(venda, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Venda> atualizarVenda(@PathVariable long id, @RequestBody Venda venda) {
        Optional<Venda> vendaBancoOpt = repositorioVenda.findById(id);

        if (venda != null && vendaBancoOpt.isPresent()) {
            Venda vendaBanco = vendaBancoOpt.get();
            vendaBanco.setIdentificacao(venda.getIdentificacao());
            vendaBanco.setCliente(venda.getCliente());
            vendaBanco.setFuncionario(venda.getFuncionario());
            vendaBanco.setMercadorias(venda.getMercadorias());
            vendaBanco.setServicos(venda.getServicos());
            vendaBanco.setVeiculo(venda.getVeiculo());

            repositorioVenda.save(vendaBanco);
            return new ResponseEntity<>(vendaBanco, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluirVenda(@PathVariable long id) {
        if (repositorioVenda.existsById(id)) {
            repositorioVenda.deleteById(id);
            return new ResponseEntity<>("Venda deletada com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Venda não encontrada!", HttpStatus.NOT_FOUND);
        }
    }
}