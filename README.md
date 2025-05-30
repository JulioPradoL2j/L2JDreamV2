# 📦 L2JDream Project - Instalação e Execução

Se você baixou o pacote do servidor e está com dúvidas de como executar, siga este passo a passo simples:

---

## 📦 Downloads Necessários

| Componente   | Download |
|--------------|----------|
| 🔧 Patch Files (System, Animations, SysTextures) | [Download Patch](https://mega.nz/file/lq8UnKhT#mO4BLffqoSEqgjF6nA34wvm67eHTkJU8Zwq9s_N9TJI) |
| ☕ Java 14 | [Download Java 14](https://mega.nz/file/xzdl0BrA#unjVTTUYbTK7-Q8wkX6Ps4b43Q5K6yOzuFGKdce3vHc) |
| 🛢️ MariaDB | [Download MariaDB](https://mega.nz/file/E6FG1LyI#uMzj3r2X1NpL9z7cINfd41Q0IRXNLgUnkcUNCd_BDtQ) |


---

## 1️⃣ Instalar o Java 14

Antes de tudo, é necessário instalar o Java 14:

- Baixe o Java 14 JDK
- Instale normalmente.

Após a instalação, **crie a variável de ambiente `JAVA_HOME`:**

### No Windows:

- Vá em: `Painel de Controle > Sistema > Configurações Avançadas > Variáveis de Ambiente`
- Clique em **Nova (variável do sistema)**
  - Nome: `JAVA_HOME`
  - Valor: `C:\Program Files\Java\jdk-14` _(ajuste conforme o local de instalação)_
- Edite a variável `Path` e adicione: `%JAVA_HOME%\bin`

### No Linux:

- Adicione no seu `~/.bashrc` ou `~/.zshrc`:
```bash
export JAVA_HOME=/caminho/para/jdk-14
export PATH=$JAVA_HOME/bin:$PATH
```

---

## 2️⃣ Instalar o MariaDB

- Baixe e instale o [MariaDB](https://mariadb.org/download/)
- Durante a instalação, defina:
  - **Usuário**: `root`
  - **Senha**: `root`

---

## 3️⃣ Rodar o banco de dados

Após a instalação do MariaDB:

- Vá até a pasta `tools/` do projeto
- Execute o script correspondente ao seu sistema operacional para importar as tabelas e dados iniciais:

### ✅ No **Windows**:
```bat
install_db.bat
```

### ✅ No **Linux**:
```bash
chmod +x install_db.sh
./install_db.sh
```

> Você também pode usar um cliente como HeidiSQL, DBeaver ou MySQL Workbench para importar manualmente os arquivos `.sql` da pasta `tools/sql`.

---

## 4️⃣ Importar o projeto no Eclipse

- Abra o **Eclipse IDE**
- Vá em: `File > Import > Existing Projects into Workspace`
- Selecione a pasta raiz do projeto
- Após a importação, procure pela pasta `launcher/`

Dentro dela estão os arquivos:

- `LoginServer.launch`
- `GameServer.launch`

Execute o servidor clicando com o botão direito no `.launch` desejado e escolha:  
**Run As > Java Application**

---

## 5️⃣ Compilar o pacote manualmente

Se quiser compilar o projeto fora do Eclipse:

- Use o `build.xml` com o Apache Ant para compilar o código-fonte
  - Ele irá gerar os `.class` e `.jar` necessários
- Depois use o `amount.xml` para:
  - Agrupar LoginServer, GameServer, Imgs, Tools e libs
  - Criar uma pasta `Zip/` com todos os arquivos prontos para execução

---

## 📌 Observações

- O projeto requer Java 14 especificamente — versões superiores podem gerar incompatibilidades.
- Certifique-se de que o MariaDB está com o serviço ativo antes de rodar o script.

---

## ❓ Problemas comuns / Common Issues

- ERRO: "mysql: command not found"
  ➤ Solução: Verifique se o MySQL/MariaDB está instalado e adicionado ao PATH.

- ERRO: Acesso negado para 'root'
  ➤ Solução: Altere a senha no script ou use um usuário com permissão.

- Nenhuma tabela é criada
  ➤ Solução: Verifique se os arquivos .sql estão realmente dentro da pasta /sql.

---

## 📬 Suporte

Caso tenha dificuldades, poste sua dúvida no fórum ou envie uma mensagem com:
- Print do erro
- Sistema operacional
- O que tentou fazer
- juliopradrol2j@gmail.com
Obrigado por usar o projeto L2JDream!
