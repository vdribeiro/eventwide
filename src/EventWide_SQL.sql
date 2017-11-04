/****************** TABLES *************/
CREATE TABLE empresas (
  username varchar(50) PRIMARY KEY,
  password varchar(50) NOT NULL,
  emp_id number UNIQUE,
  nome_empresa varchar(50),
  morada varchar(200),
  cidade varchar(50),
  gc_username varchar(70),
  gc_password varchar(70),
  gc_nome varchar(70),
  descricao varchar(500),
  tipo_conta varchar(15) DEFAULT 'user'
);

CREATE TABLE tels (
  username REFERENCES empresas(username) NOT NULL,
  telefone INTEGER NOT NULL
);

CREATE TABLE evento (
  username REFERENCES empresas(username),
  event_id number UNIQUE,
  nome varchar(100) NOT NULL,
  descricao varchar(500),
  onde varchar(100) NOT NULL,
  dinicio date NOT NULL,
  dfim date NOT NULL,
  marcacoes INTEGER DEFAULT 0,
  gc_id varchar(100),
  dalteracao varchar(100)
);

CREATE TABLE sessao (
  username REFERENCES empresas(username),
  token varchar(80) UNIQUE NOT NULL,
  dcriacao date not null
);


/************** TRIGGERS ***************/

create or replace trigger trg_autonumber
before insert on empresas
for each row
begin
  select seq_autonumber.nextval into :new.emp_id from dual;
end;

create or replace trigger trg_autonumber_evento
before insert on evento
for each row
begin
  select seq_evento_autonumber.nextval into :new.event_id from dual;
end;

create or replace trigger trg_delete_account
before delete on empresas
for each row
begin
  delete from evento where username = :old.username;
  delete from tels where username = :old.username;
  delete from sessao where username = :old.username;
end;

create or replace trigger session_date
before insert on sessao
for each row
begin
  select SYSDATE into :new.dcriacao from dual;
end;
