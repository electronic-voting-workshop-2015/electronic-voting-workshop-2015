# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20160305171556) do

  create_table "VotingPublicKeys", id: false, force: :cascade do |t|
    t.binary "content", limit: 65535
  end

  create_table "ZKP", force: :cascade do |t|
    t.integer "vote_id",  limit: 4
    t.integer "party_id", limit: 4
    t.integer "race_id",  limit: 4
    t.text    "zkp",      limit: 65535
  end

  create_table "commitments", id: false, force: :cascade do |t|
    t.binary "content", limit: 65535
  end

  create_table "complaints", id: false, force: :cascade do |t|
    t.binary "content", limit: 65535
  end

  create_table "messages", id: false, force: :cascade do |t|
    t.binary "content", limit: 65535
  end

  create_table "public_keys", primary_key: "party_id", force: :cascade do |t|
    t.integer "first",  limit: 8
    t.integer "second", limit: 8
  end

  create_table "votes", primary_key: "vote_id", force: :cascade do |t|
    t.text    "vote_value",    limit: 65535, null: false
    t.integer "ballot_box",    limit: 4,     null: false
    t.integer "serial_number", limit: 4,     null: false
    t.integer "race_id",       limit: 4,     null: false
  end

end
