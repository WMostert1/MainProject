﻿CREATE TRIGGER [trg_FarmStamp]
	ON [dbo].[Farm]
	AFTER UPDATE
	AS
	BEGIN
		SET NOCOUNT ON
		IF UPDATE(TMStamp) OR UPDATE(LastModifiedID) RETURN;
		UPDATE [dbo].[Farm]
		SET TMStamp = GETDATE(), LastModifiedID = CURRENT_USER
		WHERE FarmID IN (SELECT FarmID FROM deleted)
	END