create table Users(
    id int primary key auto_increment,
    name nvarchar(250) not null,
    email nvarchar(250) not null unique,
    password nvarchar(250) not null
);

create table Emails(
    id int primary key auto_increment,
    code nvarchar(6) not null unique,
    sender_id int not null,
    subject TINYTEXT not null,
    body TEXT,
    creation_time datetime not null,
    foreign key (sender_id) references Users(id)
);

create table Recipients(
    id int primary key auto_increment,
    recipient_id int not null,
    email_id int not null,
    is_read boolean default false,
    foreign key (recipient_id) references Users(id),
    foreign key (email_id) references Emails(id)
);