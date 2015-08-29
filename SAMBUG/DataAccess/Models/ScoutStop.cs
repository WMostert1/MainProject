//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated from a template.
//
//     Manual changes to this file may cause unexpected behavior in your application.
//     Manual changes to this file will be overwritten if the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace DataAccess.Models
{
    using System;
    using System.Collections.Generic;
    
    public partial class ScoutStop
    {
        public ScoutStop()
        {
            this.ScoutBugs = new HashSet<ScoutBug>();
        }
    
        public long ScoutStopID { get; set; }
        public long BlockID { get; set; }
        public int NumberOfTrees { get; set; }
        public float Latitude { get; set; }
        public float Longitude { get; set; }
        public System.DateTime Date { get; set; }
    
        public virtual Block Block { get; set; }
        public virtual ICollection<ScoutBug> ScoutBugs { get; set; }
    }
}
