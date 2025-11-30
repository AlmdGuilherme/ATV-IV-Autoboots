package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Veiculo;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.modelos.AdicionadorLinkVeiculo;
import com.autobots.automanager.repositorios.RepositorioVeiuculo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/veiculos")
public class VeiculoControle {
    @Autowired
    private RepositorioVeiuculo repositorioVeiuculo;
    @Autowired
    private AdicionadorLinkVeiculo adicionadorLinkVeiculo;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Veiculo>> listarVeiculos() {
        List<Veiculo> veiculos = repositorioVeiuculo.findAll();
        if (veiculos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkVeiculo.adicionadorLinkGeral(veiculos);
            return new ResponseEntity<>(veiculos, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Veiculo> buscarVeiculoPorId(@PathVariable long id) {
        Optional<Veiculo> veiculoOpt = repositorioVeiuculo.findById(id);
        if (veiculoOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Veiculo veiculo = veiculoOpt.get();
            adicionadorLinkVeiculo.adicionadorLink(veiculo);
            return new ResponseEntity<>(veiculo, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Veiculo> cadastrarVeiculo(@RequestBody Veiculo veiculo) {
        if (veiculo == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            repositorioVeiuculo.save(veiculo);
            return new ResponseEntity<>(veiculo, HttpStatus.CREATED);
        }
    }

    @PutMapping("/atualizarProprietario/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Veiculo> atualizarProprietarioVeiculo(@PathVariable long id, @RequestBody Usuario novoDono) {
        Optional<Veiculo> veiculoOpt = repositorioVeiuculo.findById(id);
        if (veiculoOpt.isPresent()) {
            Veiculo veiculo = veiculoOpt.get();
            veiculo.setProprietario(novoDono);
            repositorioVeiuculo.save(veiculo);
            return new ResponseEntity<>(veiculo, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/atualizarVendas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Veiculo> atualizarVendaVeiculo(@PathVariable long id, @RequestBody List<Venda> vendas) {
        return repositorioVeiuculo.findById(id)
                .map(veiculoSelecionado -> {
                    veiculoSelecionado.getVendas().addAll(vendas);
                    repositorioVeiuculo.save(veiculoSelecionado);
                    return new ResponseEntity<>(veiculoSelecionado, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> atualizarVeiculo(@PathVariable long id, @RequestBody Veiculo veiculo) {
        Optional<Veiculo> veiculoBancoOpt = repositorioVeiuculo.findById(id);

        if (veiculoBancoOpt.isEmpty()) {
            return new ResponseEntity<>("Veículo não encontrado", HttpStatus.NOT_FOUND);
        } else {
            if (veiculo != null) {
                Veiculo veiculoBanco = veiculoBancoOpt.get();
                veiculoBanco.setModelo(veiculo.getModelo());
                veiculoBanco.setTipo(veiculo.getTipo());
                veiculoBanco.setPlaca(veiculo.getPlaca());
                repositorioVeiuculo.save(veiculoBanco);
                return new ResponseEntity<>("Veículo atualizado com sucesso!", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Não é possível passar valores nulos (Model, Tipo e Placa)", HttpStatus.BAD_REQUEST);
            }
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluirVeiculo(@PathVariable long id) {
        if (repositorioVeiuculo.existsById(id)) {
            repositorioVeiuculo.deleteById(id);
            return new ResponseEntity<>("Veiculo removido com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Veiculo não encontrado!", HttpStatus.NOT_FOUND);
        }
    }
}