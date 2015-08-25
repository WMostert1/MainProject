﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Web.Http.Results;
using BugBusiness.Interface.BugSecurity;
using BugBusiness.Interface.BugSecurity.DTO;
using BugBusiness.Interface.BugSecurity.Exceptions;
using Newtonsoft.Json.Linq;
using DataAccess.Interface;

using System.Web;

using System.Text;
using System.Web.Mvc;

namespace BugCentral.Controllers
{
    [System.Web.Http.RoutePrefix("Synchronization")]
    public class SynchronizationController : Controller
    {
        private IDbSynchronization dbSynchronization { get; set; }
        private string json;
    
        public SynchronizationController(IDbSynchronization _dbSynchronization ) //, String _json)
        {
            dbSynchronization = _dbSynchronization;
           // json = _json;
        }

         [System.Web.Http.Route("sync")]
        public Boolean sync(string json)
        {
            try
            {
                JObject jsonFile = JObject.Parse(json);

                JObject actualData = JObject.Parse(jsonFile["syncData"].ToString());

                foreach (var scoutStop in actualData["ScoutStops"])
                {
                    Int64 scoutStopID = Convert.ToInt64(scoutStop["ScoutStopID"].ToString());
                    Int64 userID = Convert.ToInt64(scoutStop["UserID"].ToString());
                    Int64 blockID = Convert.ToInt64(scoutStop["BlockID"].ToString());
                    int numberOfTrees = Convert.ToInt16(scoutStop["NumberOfTrees"].ToString());
                    float latitude = float.Parse(scoutStop["Latitude"].ToString().Replace(".", ","));
                    float longitude = float.Parse(scoutStop["Longitude"].ToString().Replace(".", ","));
                    DateTime date = Convert.ToDateTime(scoutStop["Date"].ToString());
                    int lastModifiedID = Convert.ToInt16(scoutStop["LastModifiedID"].ToString());
                    DateTime tmStamp = Convert.ToDateTime(scoutStop["TMStamp"].ToString());

                    dbSynchronization.PersistBugStops(scoutStopID, userID, blockID, numberOfTrees, latitude, longitude, date, lastModifiedID, tmStamp);
                }

                foreach (var scoutBugs in actualData["ScoutBugs"])
                {
                    Int64 scoutBugID = Convert.ToInt64(scoutBugs["ScoutBugID"].ToString());
                    Int64 scoutStopID = Convert.ToInt64(scoutBugs["ScoutStopID"].ToString());
                    Int64 speciesID = Convert.ToInt64(scoutBugs["SpeciesID"].ToString());
                    int numberOfBugs = Convert.ToInt16(scoutBugs["NumberOfBugs"].ToString());
                    byte[] fieldPicture = Encoding.ASCII.GetBytes(scoutBugs["FieldPicture"].ToString());
                    String comments = scoutBugs["Comments"].ToString();
                    int lastModifiedID = Convert.ToInt16(scoutBugs["LastModifiedID"].ToString());
                    DateTime tmStamp = Convert.ToDateTime(scoutBugs["TMStamp"].ToString());

                    dbSynchronization.PersistScoutBugs(scoutBugID, scoutStopID, speciesID, numberOfBugs, fieldPicture, comments, lastModifiedID, tmStamp);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                return false;
            }

            return true;

        }
    }
}