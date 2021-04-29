<@import "../a" />
<@import "./b" />
<@import "./y/c" />
<@import "../x/y/../b" "b2" />
Template with relative imports
<@a.ma />
<@b.mb />
<@c.mc />
<@b2.mb />