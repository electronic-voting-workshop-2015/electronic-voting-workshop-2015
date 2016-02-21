ENV['RACK_ENV'] = 'test'
require 'minitest/autorun'
require 'rack/test'
require 'app/app'

module TestHelper
    def randomString( length: 10 )
        alaphabet = 'abcdefghijklmnopqrtst0123456789!@#$%^&*'.split( '' )
        array = alaphabet.sample( length )
        array.join( '' )
    end

    def randomInteger
        (Random.rand * 10000000).to_i
    end
end
