//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated from a template.
//
//     Manual changes to this file may cause unexpected behavior in your application.
//     Manual changes to this file will be overwritten if the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace DataAccess.MSSQL
{
    using System;
    using System.Collections.Generic;
    
    public partial class Block
    {
        public Block()
        {
            this.ScoutStops = new HashSet<ScoutStop>();
            this.Treatments = new HashSet<Treatment>();
        }
    
        public int BlockID { get; set; }
        public int FarmID { get; set; }
        public string BlockName { get; set; }
        public Nullable<int> LastModifiedID { get; set; }
        public Nullable<System.DateTime> TMStamp { get; set; }
    
        public virtual Farm Farm { get; set; }
        public virtual ICollection<ScoutStop> ScoutStops { get; set; }
        public virtual ICollection<Treatment> Treatments { get; set; }
    }
}
