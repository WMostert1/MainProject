﻿CREATE TABLE [dbo].[Block]
(
	[BlockID] BIGINT NOT NULL PRIMARY KEY IDENTITY ,
	[FarmID] BIGINT NOT NULL,
	[BlockName] VARCHAR(50) NOT NULL,
	[LastModifiedID] SYSNAME DEFAULT CURRENT_USER NOT NULL,
	[TMStamp] DATETIME DEFAULT GETDATE() NOT NULL	
	CONSTRAINT [FK_Farm_ToBlock] FOREIGN KEY (FarmID) REFERENCES [Farm]([FarmID]) ON DELETE CASCADE
)