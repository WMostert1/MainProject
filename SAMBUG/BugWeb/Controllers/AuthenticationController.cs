﻿using System.Web.Mvc;
using BugBusiness.Interface.BugSecurity;
using BugBusiness.BugAuthentication;
using BugBusiness.Interface.BugSecurity.DTO;
using BugBusiness.Interface.BugSecurity.Exceptions;
using BugWeb.Models;
using BugWeb.Security;
using BugBusiness.Interface.BugAuthentication;
using BugBusiness.Interface.BugAuthentication.DTO;

namespace BugWeb.Controllers
{
    public class AuthenticationController : Controller
    {
        private readonly IBugSecurity _bugSecurity;
        private readonly IBugAuthentication _bugAuthentication;

        public AuthenticationController(IBugSecurity bugSecurity)
        {
            _bugSecurity = bugSecurity;
            _bugAuthentication = new BugAuthentication(_bugSecurity);
        }

        [HttpPost]
        public ActionResult Login(LoginViewModel loginViewModel)
        {

            LoginRequest loginRequest = new LoginRequest()
            {
                Username = loginViewModel.Username,
                Password = loginViewModel.Password
            }; 

            try
            {
                LoginResponse loginResponse = _bugSecurity.Login(loginRequest);

                //set up session
               Session["UserInfo"] = loginResponse.User;
                return Json(new
                {
                    success = true,
                    isGrower = SecurityProvider.isGrower(Session),
                    isAdmin = SecurityProvider.isAdmin(Session)
                });
                }
            catch (NotRegisteredException)
                {
                return Json(new {success = false});
                }
            }

        public ActionResult Logout()
            {
            Session.Abandon();
            return RedirectToAction("index", "home");
            }

        [HttpPost]
        public ActionResult Register(RegisterViewModel registerViewModel)
        {
            RegisterRequest registerRequest = new RegisterRequest()
            {
                Username = registerViewModel.Username,
                UsernameConfirmation = registerViewModel.UsernameConfirmation,
                Password = registerViewModel.Password,
                PasswordConfirmation = registerViewModel.PasswordConfirmation,
            };

            try
            {
                _bugSecurity.Register(registerRequest);

                //automatically log in
                LoginResponse loginResponse = _bugSecurity.Login(new LoginRequest()
                {
                    Username = registerRequest.Username,
                    Password = registerRequest.Password
                });

                //set up session
                Session["UserInfo"] = loginResponse.User;

                //todo: factorise these Json object (define a DTO)
                return Json( new {
                    success = true,
                    invalidInputError = false,
                    userExistsError = false,
                    isGrower = SecurityProvider.isGrower(Session),
                    isAdmin = SecurityProvider.isAdmin(Session)
                });
            }
            catch (InvalidInputException)
            {
                return Json(new
                {
                    success = false,
                    invalidInputError = true,
                    userExistsError = false,
                    isGrower = false,
                    isAdmin = false
                });
            }
            catch (UserExistsException)
            {
                return Json(new
                {
                    success = false,
                    invalidInputError = false,
                    userExistsError = true,
                    isGrower = false,
                    isAdmin = false
                });
            }

        }

        [HttpGet]
        public ActionResult EditUserRoles()
        {
            if (!SecurityProvider.isAdmin(Session))
                return View("~/Views/Shared/Error.cshtml");

            return View();
        }

        [HttpPost]
        public ActionResult EditUserRoles(EditUserRoleViewModel editUserRoleViewModel)
        {
            if (!SecurityProvider.isAdmin(Session))
                return View("~/Views/Shared/Error.cshtml");

            return RedirectToAction("EditUserRoles","Authentication");
        }

      
        //[HttpGet]
        public ActionResult CheckEmail()
        {
            return View("~/Views/Authentication/CheckEmail.cshtml");
        }

        public ActionResult ChangePassword(ChangePasswordModel changePasswordModel)
        {


            ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest()
            {
                Email = changePasswordModel.Email,
                Password = changePasswordModel.Password
            }; 
            _bugAuthentication.ChangePassword(changePasswordRequest);
            return RedirectToAction("login", "home");
        }

    }
}