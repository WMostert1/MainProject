﻿CREATE TABLE [dbo].[Species]
(
	[SpeciesID] BIGINT NOT NULL PRIMARY KEY IDENTITY ,
	[SpeciesName] VARCHAR(50) NOT NULL,
	[Lifestage] INT NOT NULL,
	[IdealPicture] VARBINARY(MAX) NOT NULL,
	[IsPest] BIT NOT NULL,
	[LastModifiedID] SYSNAME DEFAULT CURRENT_USER NOT NULL,
	[TMStamp] DATETIME DEFAULT GETDATE() NOT NULL	
)
