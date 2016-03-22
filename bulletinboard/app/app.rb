require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'

use Rack::PostBodyContentTypeParser

require 'app/configuration'
require 'app/publish_and_retrieve'
require 'app/messages'
require 'app/public_key'
#remove comment below to include 'votes' code
require 'app/votes'
require 'app/security'
