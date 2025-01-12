﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Web.Http;
using System.Web.Http.Results;
using BugBusiness.Interface.BugSecurity;
using BugBusiness.Interface.BugSecurity.DTO;
using BugBusiness.Interface.BugSecurity.Exceptions;
using Newtonsoft.Json.Linq;
using System.IO;
using System.Text;

namespace BugWeb.Controllers
{
    [RoutePrefix("api/authentication")]
    public class ApiAuthenticationController : ApiController
    {
        private readonly IBugSecurity _bugSecurity;

        public ApiAuthenticationController(IBugSecurity bugSecurity)
        {
            _bugSecurity = bugSecurity;
        }

       [HttpPost]
        [Route("login")]
        public LoginResponse Login([FromBody] LoginRequest loginRequest)
        {
            try
            {
                LoginResponse loginResponse = _bugSecurity.Login(loginRequest);
                return loginResponse;
            }
            catch (NotRegisteredException)
            {
                throw new HttpResponseException(HttpStatusCode.Unauthorized);
            }
        }

        [HttpPost]
        [Route("register")]
        public RegisterResponse Register([FromBody] RegisterRequest registerRequest)
        {
            try
            {
                RegisterResponse registerResponse = _bugSecurity.Register(registerRequest);
                return registerResponse;
            }
            catch (UserExistsException)
            {
                throw new HttpResponseException(HttpStatusCode.PreconditionFailed);
            }
        }

        [HttpPost]
        [Route("recoveraccount")]
        public void RecoverAccount(RecoverAccountRequest recoverAccountRequest)
        {
            _bugSecurity.RecoverAccount(recoverAccountRequest);

        }

        [HttpPost]
        [Route("registerdevice")]
        public RegisterDeviceResponse RegisterDevice([FromBody] RegisterDeviceRequest request)
        {

            try
            {

                return _bugSecurity.RegisterDevice(request);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.StackTrace);
                return new RegisterDeviceResponse() { Registered = false };
            }
        }

        [HttpPost]
        [Route("updateuserdevice")]
        public UpdateUserDeviceResponse UpdateUserDevice(UpdateUserDeviceRequest updateuserdevicerequest)
        {
            try
            {
                return _bugSecurity.UpdateUserDevice(updateuserdevicerequest);
            }
            catch(Exception ex)
            {
                Console.Write(ex.StackTrace);
                return new UpdateUserDeviceResponse { Result = -1 };
            }
        }


    }
}
