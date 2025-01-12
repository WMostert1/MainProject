﻿using System;
using System.CodeDom;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using BugBusiness.Interface.BugSecurity.DTO;

namespace BugBusiness.Interface.BugSecurity
{
    public interface IBugSecurity
    {
        LoginResponse Login(LoginRequest loginRequest);
        RegisterResponse Register(RegisterRequest registerRequest);
        RecoverAccountResponse RecoverAccount(RecoverAccountRequest recoverAccountRequest);
        GetUsersResponse GetUsers();
        EditUserRoleResponse EditUserRoles(EditUserRoleRequest editUserRoleRequest);
        bool ChangeUserPassword(string username, string password);
        String GetPassword(string username);
        RegisterDeviceResponse RegisterDevice(RegisterDeviceRequest request);
        UpdateUserDeviceResponse UpdateUserDevice(UpdateUserDeviceRequest updateUserDeviceRequest);
    }
}
