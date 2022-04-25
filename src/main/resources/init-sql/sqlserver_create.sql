-- noinspection SqlNoDataSourceInspectionForFile

-- ----------------------------
-- create table
-- ----------------------------
CREATE TABLE [dbo].[trigger_deleted] (
                                         [id] bigint  IDENTITY(1,1) NOT NULL,
                                         [docID] varchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
                                         [type] varchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
                                         [delTime] datetime  NULL,
                                         [comment] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[trigger_deleted] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Indexes structure for table trigger_deleted
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_delTime_type]
    ON [dbo].[trigger_deleted] (
                                [delTime] ASC,
                                [type] ASC
        )
    INCLUDE ([docID])
GO


-- ----------------------------
-- Primary Key structure for table trigger_deleted
-- ----------------------------
ALTER TABLE [dbo].[trigger_deleted] ADD CONSTRAINT [PK__trigger___3213E83F43BFE8E7] PRIMARY KEY CLUSTERED ([id])
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
GO

-- ----------------------------
-- create trigger for big_data
-- ----------------------------
create trigger trigger_big_data_delete  --触发器名称
    on big_data after delete
    as
begin
    INSERT INTO trigger_deleted (docID,type,delTime)
    SELECT
        id,
        'big_1',
        SYSDATETIME()
    FROM
        deleted;
end
