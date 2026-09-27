-- Tabela DEAL
CREATE TABLE deal (
                      deal_id               VARCHAR(100)  NOT NULL,
                      title                 VARCHAR(500)  NOT NULL,
                      steam_app_id          VARCHAR(50)   NOT NULL,
                      steam_rating_percent  VARCHAR(10)   NOT NULL,
                      status                VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
                      creation_date         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT pk_deal PRIMARY KEY (deal_id)
);

-- Tabela MESSAGE
CREATE TABLE message (
                         message_id      BIGINT        GENERATED ALWAYS AS IDENTITY,
                         deal_id         VARCHAR(100)  NOT NULL,
                         send_date       TIMESTAMP,
                         creation_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         message_status  VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
                         source_type     VARCHAR(20)   NOT NULL,
                         number          VARCHAR(50)   NOT NULL,
                         mediatype       VARCHAR(20)   NOT NULL,
                         mimetype        VARCHAR(50)   NOT NULL,
                         media           VARCHAR(500)  NOT NULL,
                         caption         TEXT          NOT NULL,
                         CONSTRAINT pk_message PRIMARY KEY (message_id),
                         CONSTRAINT fk_message_deal FOREIGN KEY (deal_id) REFERENCES deal (deal_id),
                         CONSTRAINT uq_message_deal UNIQUE (deal_id)
);