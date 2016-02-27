require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'
require 'pp'

use Rack::PostBodyContentTypeParser

require 'app/publish_and_retrieve'
#remove comment below to include 'votes' code
#require 'app/votes'
